package gov.hhs.onc.phiz.web.crypto.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import javax.annotation.Resource;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

@Configurable
@Lazy
public class PhizJsseImplementation extends JSSEImplementation implements InitializingBean {
    private class PhizJsseSocketFactory implements ServerSocketFactory, SSLUtil {
        private AbstractEndpoint<?> endpoint;

        public PhizJsseSocketFactory(AbstractEndpoint<?> endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void handshake(Socket socket) throws IOException {
            SSLSocket sslSocket = ((SSLSocket) socket);
            SSLSession session = sslSocket.getSession();

            if (session.getCipherSuite().equals(HANDSHAKE_FAILED_CIPHER_SUITE)) {
                throw new IOException(String.format("SSL socket (remoteAddr=%s) handshake failed.", socket.getRemoteSocketAddress()));
            }

            sslSocket.setEnabledCipherSuites(new String[0]);
        }

        @Override
        public Socket acceptSocket(ServerSocket serverSocket) throws IOException {
            try {
                return serverSocket.accept();
            } catch (SSLException e) {
                throw new SocketException(String.format("SSL server socket (addr=%s) handshake failed: %s", serverSocket.getLocalSocketAddress(),
                    e.getMessage()));
            }
        }

        @Override
        public SSLContext createSSLContext() throws Exception {
            return PhizJsseImplementation.this.context;
        }

        @Override
        public ServerSocket createSocket(int port) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.serverSocketFactory.createServerSocket(port);
        }

        @Override
        public ServerSocket createSocket(int port, int backlog) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.serverSocketFactory.createServerSocket(port, backlog);
        }

        @Override
        public ServerSocket createSocket(int port, int backlog, InetAddress interfaceAddr) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.serverSocketFactory.createServerSocket(port, backlog, interfaceAddr);
        }

        @Override
        public String[] getEnableableCiphers(SSLContext context) {
            return PhizJsseImplementation.this.params.getCipherSuites();
        }

        @Override
        public String[] getEnableableProtocols(SSLContext context) {
            return PhizJsseImplementation.this.params.getProtocols();
        }

        @Override
        public KeyManager[] getKeyManagers() throws Exception {
            return PhizJsseImplementation.this.keyManagers;
        }

        @Override
        public TrustManager[] getTrustManagers() throws Exception {
            return PhizJsseImplementation.this.trustManagers;
        }

        @Override
        public void configureSessionContext(SSLSessionContext sslSessionContext) {
            sslSessionContext.setSessionCacheSize(1);
            sslSessionContext.setSessionTimeout(5);
        }

        private void initializeSession() {
            this.configureSessionContext(PhizJsseImplementation.this.context.getServerSessionContext());
        }
    }

    private final static String HANDSHAKE_FAILED_CIPHER_SUITE = "SSL_NULL_WITH_NULL_NULL";

    private final static String IMPL_NAME = "PHIZ JSSE";

    @Resource(name = "keyManagerTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private KeyManager[] keyManagers;

    @Resource(name = "trustManagerTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private TrustManager[] trustManagers;

    @Resource(name = "sslParamsServerTomcatServer")
    private SSLParameters params;

    @Resource(name = "sslContextTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private SSLContext context;

    private SSLServerSocketFactory serverSocketFactory;

    @Override
    public ServerSocketFactory getServerSocketFactory(AbstractEndpoint<?> endpoint) {
        return new PhizJsseSocketFactory(endpoint);
    }

    @Override
    public SSLUtil getSSLUtil(AbstractEndpoint<?> endpoint) {
        return new PhizJsseSocketFactory(endpoint);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.serverSocketFactory = this.context.getServerSocketFactory();
    }

    @Override
    public String getImplementationName() {
        return IMPL_NAME;
    }
}
