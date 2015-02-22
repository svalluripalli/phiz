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
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import org.apache.tomcat.util.net.AbstractEndpoint;
import org.apache.tomcat.util.net.SSLUtil;
import org.apache.tomcat.util.net.ServerSocketFactory;
import org.apache.tomcat.util.net.jsse.JSSEImplementation;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.annotation.Lazy;

@Configurable
@Lazy
public class PhizJsseImplementation extends JSSEImplementation {
    private class PhizJsseSocketFactory implements ServerSocketFactory, SSLUtil {
        private AbstractEndpoint<?> endpoint;

        public PhizJsseSocketFactory(AbstractEndpoint<?> endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void handshake(Socket socket) throws IOException {
            ((SSLSocket) socket).getSession();
        }

        @Override
        public Socket acceptSocket(ServerSocket socket) throws IOException {
            try {
                return socket.accept();
            } catch (SSLException e) {
                throw new SocketException(String.format("Unable to accept SSL socket: %s", e.getMessage()));
            }
        }

        @Override
        public SSLContext createSSLContext() throws Exception {
            return PhizJsseImplementation.this.sslContext;
        }

        @Override
        public ServerSocket createSocket(int port) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.sslServerSocketFactory.createServerSocket(port);
        }

        @Override
        public ServerSocket createSocket(int port, int backlog) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.sslServerSocketFactory.createServerSocket(port, backlog);
        }

        @Override
        public ServerSocket createSocket(int port, int backlog, InetAddress interfaceAddr) throws IOException {
            this.initializeSession();

            return PhizJsseImplementation.this.sslServerSocketFactory.createServerSocket(port, backlog, interfaceAddr);
        }

        @Override
        public String[] getEnableableCiphers(SSLContext context) {
            return PhizJsseImplementation.this.sslParams.getCipherSuites();
        }

        @Override
        public String[] getEnableableProtocols(SSLContext context) {
            return PhizJsseImplementation.this.sslParams.getProtocols();
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
            this.configureSessionContext(PhizJsseImplementation.this.sslContext.getServerSessionContext());
        }
    }

    private final static String IMPL_NAME = "PHIZ JSSE";

    @Resource(name = "keyManagerTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private KeyManager[] keyManagers;

    @Resource(name = "trustManagerTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private TrustManager[] trustManagers;

    @Resource(name = "sslParamsServerTomcatServer")
    private SSLParameters sslParams;

    @Resource(name = "sslContextTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private SSLContext sslContext;

    @Resource(name = "sslServerSocketFactoryTomcatServer")
    @SuppressWarnings({ "SpringJavaAutowiringInspection" })
    private SSLServerSocketFactory sslServerSocketFactory;

    @Override
    public ServerSocketFactory getServerSocketFactory(AbstractEndpoint<?> endpoint) {
        return new PhizJsseSocketFactory(endpoint);
    }

    @Override
    public SSLUtil getSSLUtil(AbstractEndpoint<?> endpoint) {
        return new PhizJsseSocketFactory(endpoint);
    }

    @Override
    public String getImplementationName() {
        return IMPL_NAME;
    }
}
