package ru.spbau.sergeev.btrack.common;

import ru.spbau.sergeev.btrack.common.messages.Message;
import ru.spbau.sergeev.btrack.common.messages.Serializer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author pavel
 */
public abstract class Actor implements Runnable {
    private static Logger log = Logger.getLogger(Actor.class.getName());

    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final ServerSocketChannel serverChannel;
    private final Selector selector;
    private final List<ChangeRequest> pendingChanges = new LinkedList<>();
    private final ConcurrentMap<SocketChannel, ConcurrentLinkedQueue<ByteBuffer>> pendingData = new ConcurrentHashMap<>();
    private boolean isRunning = true;

    private static class ChangeRequest {
        public enum Type {
            REGISTER,
            CLOSE,
            CHANGEOPS
        }

        public final SocketChannel socket;
        public final Type type;
        public final int ops;

        public ChangeRequest(SocketChannel socket, Type type, int ops) {
            this.socket = socket;
            this.type = type;
            this.ops = ops;
        }
    }

    public Actor(InetSocketAddress isa) throws IOException {
        log.setLevel(Level.SEVERE);
        selector = SelectorProvider.provider().openSelector();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(isa);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    /**
     * Calls when Actor started
     * Calls in non selector thread
     *
     */
    public abstract void onStart();

    /**
     * Calls on new incoming message
     * Calls in non selector thread
     *
     * @param msg
     * @param socketChannel
     */
    public abstract void processMessage(Message msg, SocketChannel socketChannel);

    /**
     * Calls on connecting finished
     * Calls in non selector thread
     *
     * @param socketChannel
     */
    public abstract void onConnectingFinished(SocketChannel socketChannel);


    /**
     * Calls on disconnecting
     * Calls in non selector thread
     *
     * @param socketChannel
     */
    public abstract void onDisconnect(SocketChannel socketChannel);

    public abstract void onShutdown();

    public abstract void onConnectingError(SocketChannel socketChannel);

    /**
     * Initiate new connection.
     * Can be called in non selector thread.
     *
     * @param targetISA
     * @throws IOException
     */
    public SocketChannel initiateConnection(InetSocketAddress targetISA) throws IOException {
        log.log(Level.INFO, String.format("Try to initiate connection to %s", targetISA));
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        socketChannel.connect(targetISA);

        synchronized (pendingChanges) {
            pendingChanges.add(new ChangeRequest(socketChannel, ChangeRequest.Type.REGISTER, SelectionKey.OP_CONNECT));
        }
        selector.wakeup();
        return socketChannel;
    }

    /**
     * Close connection.
     * Can be called in non selector thread.
     */
    public void closeConnection(SocketChannel socket) {
        synchronized (pendingChanges) {
            pendingChanges.add(new ChangeRequest(socket, ChangeRequest.Type.CLOSE, 0));
        }
        selector.wakeup();
    }

    /**
     * Send message
     * Can be called in non selector thread.
     *
     * @param socket
     * @param msg
     */
    public void sendMessage(SocketChannel socket, Message msg) throws IOException {
        log.log(Level.INFO, "Request add to queue");
        addMessageToQueue(socket, msg);
        synchronized (pendingChanges) {
            pendingChanges.add(new ChangeRequest(socket, ChangeRequest.Type.CHANGEOPS, SelectionKey.OP_WRITE));
        }
        selector.wakeup();
    }

    public void shutDown() {
        log.log(Level.INFO, "Shutdown actor");
        isRunning = false;
        selector.wakeup();
        executor.shutdown();
        onShutdown();
    }

    private void accept(SelectionKey key) throws IOException {
        log.log(Level.INFO, "Incoming connection");
        SocketChannel socketChanel = serverChannel.accept();
        socketChanel.configureBlocking(false);
        socketChanel.register(selector, SelectionKey.OP_READ);
    }

    private void read(SelectionKey key) {
        log.log(Level.INFO, "Data reading");
        final SocketChannel socketChanel = (SocketChannel) key.channel();
        Message msg;
        try {
            msg = readMessage(socketChanel);
        } catch (Exception e) {
            key.cancel();
            try {
                socketChanel.close();
            } catch (IOException ne) {
                log.log(Level.SEVERE, "Error when closing chanel", ne);
            }
            executor.submit(() -> onDisconnect((SocketChannel) key.channel()));
            log.log(Level.INFO, "Data not reading");
            return;
        }

        log.log(Level.INFO, "Message received");
        executor.submit(() -> processMessage(msg, socketChanel));
    }

    private void write(SelectionKey key) throws IOException {
        log.log(Level.INFO, "Data writing");
        final SocketChannel socketChanel = (SocketChannel)key.channel();
        ConcurrentLinkedQueue<ByteBuffer> queue = pendingData.get(socketChanel);
        while (true) {
            final ByteBuffer buf = queue.poll();
            if (buf == null) break;
            while (buf.remaining() > 0) {
                log.log(Level.INFO, String.format("Write remaining: %d", buf.remaining()));
                socketChanel.write(buf);
            }
        }
        if (queue.isEmpty()) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void finishConnecting(SelectionKey key) {
        log.log(Level.INFO, "Connecting finishing");
        SocketChannel socketChannel = (SocketChannel) key.channel();
        try {
            if (!socketChannel.finishConnect()) {
                log.log(Level.INFO, "connecting failed");
                executor.submit(() -> onConnectingError((SocketChannel) key.channel()));
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, "Error when connecting finishing", e);
            key.cancel();
            return;
        }

        executor.submit(() -> onConnectingFinished(socketChannel));
        log.log(Level.INFO, "Connecting finishing successfully");
    }

    private int readMessageSize(SocketChannel socketChannel) throws IOException {
        final ByteBuffer messageSizeBuffer = ByteBuffer.allocate(4);
        final int numRead = socketChannel.read(messageSizeBuffer);
        if (numRead == -1) {
            log.log(Level.INFO, "End of stream reached");
            return 0;
        }
//        if (numRead != 4) { // TODO
//            log.log(Level.SEVERE, "Message size not found");
//            throw new RuntimeException("Wrong message exception");
//        }
        while (messageSizeBuffer.hasRemaining()) {
            socketChannel.read(messageSizeBuffer);
        }
        log.log(Level.INFO, String.format("Message size %d bytes red", numRead));
        messageSizeBuffer.flip();
        log.log(Level.INFO, String.format("Remaining %d bytes", messageSizeBuffer.remaining()));
        return messageSizeBuffer.getInt();
    }

    private Message readMessage(SocketChannel socketChannel) throws IOException, ClassNotFoundException {
        final int messageSize = readMessageSize(socketChannel);
        log.log(Level.INFO, String.format("Message size: %d", messageSize));
        if (messageSize <= 0) {
            throw new RuntimeException("Wrong message size");
        }
        final ByteBuffer messageBuffer = ByteBuffer.allocate(messageSize);
        while (messageBuffer.hasRemaining()) { // TODO this loop blocking main thread
            socketChannel.read(messageBuffer);
        }
        log.log(Level.INFO, String.format("Message %d bytes red", messageSize));
        return Serializer.deserialize(messageBuffer.array());
    }

    private void addMessageToQueue(SocketChannel socket, Message msg) throws IOException {
        pendingData.putIfAbsent(socket, new ConcurrentLinkedQueue<>());
        ConcurrentLinkedQueue<ByteBuffer> queue = pendingData.get(socket);
        byte[] buffer = Serializer.serialize(msg);
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 + buffer.length);
        byteBuffer.putInt(buffer.length);
        byteBuffer.put(buffer);
        byteBuffer.flip();
        queue.offer(byteBuffer);
        log.log(Level.INFO, String.format("Message added to queue: %d", buffer.length));
    }

    @Override
    public void run() {
        log.log(Level.INFO, "Actor started");
        executor.submit(() -> onStart());

        while (isRunning) {
            try {
                synchronized (pendingChanges) {
                    for (final ChangeRequest change : pendingChanges) {
                        switch (change.type) {
                            case REGISTER:
                                change.socket.register(selector, change.ops);
                                log.log(Level.INFO, "Register priority");
                                break;
                            case CLOSE:
                                change.socket.close();
                                change.socket.keyFor(selector).cancel();
                                log.log(Level.INFO, "Close priority");
                                break;
                            case CHANGEOPS:
                                SelectionKey key = change.socket.keyFor(selector);
                                key.interestOps(change.ops);
                                log.log(Level.INFO, String.format("Ops changed to %d", change.ops));
                                break;
                        }
                    }
                    pendingChanges.clear();
                }

                int selectedKeys = selector.select();
                //log.log(Level.INFO, String.format("selected keys: %d", selectedKeys));
                for (final SelectionKey key : selector.selectedKeys()) {
                    if (!key.isValid()) {
                        continue;
                    }

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isReadable()) {
                        read(key);
                    } else if (key.isWritable()) {
                        write(key);
                    } else if (key.isConnectable()) {
                        finishConnecting(key);
                    }
                }
                selector.selectedKeys().clear();
            } catch (IOException e) {
                log.log(Level.SEVERE, "Error in selector loop", e);
            }
        }
        log.log(Level.INFO, "Selector loop terminated");
    }
}
