/**
 * Copyright (C) 2002,2005 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: IrmiPRODelegate.java,v 1.2 2005-10-19 14:45:01 benoitf Exp $
 * --------------------------------------------------------------------------
 */
package org.objectweb.carol.rmi.multi;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.objectweb.carol.irmi.ClientInterceptor;
import org.objectweb.carol.irmi.Interceptor;
import org.objectweb.carol.irmi.PRO;
import org.objectweb.carol.irmi.Server;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JInterceptorStore;
import org.objectweb.carol.rmi.jrmp.interceptor.JRMPClientRequestInfoImpl;
import org.objectweb.carol.rmi.jrmp.interceptor.JRMPServerRequestInfoImpl;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JServiceContext;
import org.objectweb.carol.rmi.util.PortNumber;
import org.objectweb.carol.util.configuration.CarolDefaultValues;
import org.objectweb.carol.util.configuration.ConfigurationRepository;

/**
 * IrmiPRODelegate
 *
 * @author Rafael H. Schloming &lt;rhs@mit.edu&gt;
 **/

public class IrmiPRODelegate extends PRO {

    private static class ServerInterceptorImpl implements Interceptor {

        private JServerRequestInterceptor[] sis;

        public ServerInterceptorImpl(JServerRequestInterceptor[] sis) {
            this.sis = sis;
        }

        public void receive(byte code, ObjectInput in)
            throws IOException, ClassNotFoundException {
            JServerRequestInfo info = new JRMPServerRequestInfoImpl();
            int len = in.readShort();
            for (int i = 0; i < len; i++) {
                info.add_reply_service_context((JServiceContext) in.readObject());
            }
            for (int i = 0; i < sis.length; i++) {
                sis[i].receive_request(info);
            }
        }

        public void send(byte code, ObjectOutput out) throws IOException {
            JServerRequestInfo info = new JRMPServerRequestInfoImpl();
            for (int i = 0; i < sis.length; i++) {
                switch (code) {
                case METHOD_RESULT:
                    sis[i].send_reply(info);
                    break;
                case METHOD_ERROR:
                case SYSTEM_ERROR:
                    sis[i].send_exception(info);
                    break;
                }
            }
            Collection c = info.get_all_reply_service_context();
            out.writeShort(c.size());
            for (Iterator it = c.iterator(); it.hasNext(); ) {
                out.writeObject(it.next());
            }
        }

    }

    private static class ClientInterceptorImpl implements ClientInterceptor {

        private JClientRequestInterceptor[] cis;

        public ClientInterceptorImpl(JClientRequestInterceptor[] cis) {
            this.cis = cis;
        }

        public void send(byte code, ObjectOutput out) throws IOException {
            JClientRequestInfo info = new JRMPClientRequestInfoImpl();
            for (int i = 0; i < cis.length; i++) {
                cis[i].send_request(info);
            }
            Collection c = info.get_all_request_service_context();
            out.writeShort(c.size());
            for (Iterator it = c.iterator(); it.hasNext(); ) {
                out.writeObject(it.next());
            }
        }

        public void receive(byte code, ObjectInput in)
            throws IOException, ClassNotFoundException {
            JClientRequestInfo info = new JRMPClientRequestInfoImpl();
            int len = in.readShort();
            for (int i = 0; i < len; i++) {
                info.add_request_service_context((JServiceContext) in.readObject());
            }
            for (int i = 0; i < cis.length; i++) {
                switch (code) {
                case METHOD_RESULT:
                    cis[i].receive_reply(info);
                    break;
                case METHOD_ERROR:
                case SYSTEM_ERROR:
                    cis[i].receive_exception(info);
                    break;
                }
            }
        }

    }

    private static Server getServer(boolean usingCmi) {
        int port = 0;
        Properties prop = ConfigurationRepository.getProperties();
        if (!usingCmi && prop != null) {
            String propertyName = CarolDefaultValues.SERVER_JRMP_PORT;
            port = PortNumber.strToint(prop.getProperty(propertyName, "0"), propertyName);
        }
        return new Server(port, new ClientInterceptorImpl(JInterceptorStore.getLocalClientInterceptors()),
                          new ServerInterceptorImpl(JInterceptorStore.getLocalServerInterceptors()));
    }

    public IrmiPRODelegate(boolean usingCmi) {
        super(getServer(usingCmi));
    }

    public IrmiPRODelegate() {
        this(false);
    }

}
