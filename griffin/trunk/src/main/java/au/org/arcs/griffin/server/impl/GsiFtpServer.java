package au.org.arcs.griffin.server.impl;


import java.util.ResourceBundle;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.GSIConstants;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.gsi.TrustedCertificates;
import org.globus.gsi.gssapi.GSSConstants;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.gridforum.jgss.ExtendedGSSContext;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;

import au.org.arcs.griffin.common.FtpSessionContext;
import au.org.arcs.griffin.session.impl.FtpSessionContextImpl;

public class GsiFtpServer extends DefaultFtpServer {
    private static Log log = LogFactory.getLog(GsiFtpServer.class);

    protected FtpSessionContext createFtpContext() {
        FtpSessionContext ctx = new FtpSessionContextImpl(getOptions(), getFileSystem(), ResourceBundle
            .getBundle(getResources()), this);
        ctx.setAttribute(ATTR_SSL, Boolean.FALSE);
        return ctx;
    }

}
