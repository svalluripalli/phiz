package gov.hhs.onc.phiz.web.ws.servlet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.transport.servlet.CXFNonSpringServlet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

public class PhizCxfServlet extends CXFNonSpringServlet implements InitializingBean {
    private final static String X_ICON_CONTENT_TYPE = new MediaType("image", "x-icon").toString();

    private final static String FAVICON_URL_PATH = "/favicon.ico";

    private final static long serialVersionUID = 0L;

    private FileSystemResource faviconResource;
    private byte[] faviconContent;

    public PhizCxfServlet() {
        super(null, false);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        try (InputStream faviconInStream = this.faviconResource.getInputStream()) {
            this.faviconContent = IOUtils.toByteArray(faviconInStream);
        }
    }

    @Override
    protected void handleRequest(HttpServletRequest servletReq, HttpServletResponse servletResp) throws ServletException {
        if (!Objects.equals(servletReq.getPathInfo(), FAVICON_URL_PATH)) {
            this.invoke(servletReq, servletResp);

            return;
        }

        servletResp.setContentType(X_ICON_CONTENT_TYPE);

        try {
            OutputStream servletRespOutStream = servletResp.getOutputStream();
            servletRespOutStream.write(this.faviconContent);
            servletRespOutStream.flush();
        } catch (IOException e) {
            throw new ServletException(String.format("Unable to write favicon (urlPath=%s) servlet response.", FAVICON_URL_PATH), e);
        }
    }

    @Override
    protected void finalizeServletInit(ServletConfig servletConfig) {
    }

    public FileSystemResource getFaviconResource() {
        return this.faviconResource;
    }

    public void setFaviconResource(FileSystemResource faviconResource) {
        this.faviconResource = faviconResource;
    }
}
