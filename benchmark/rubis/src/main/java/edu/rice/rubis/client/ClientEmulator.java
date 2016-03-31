/*
 * RUBiS
 * Copyright (C) 2002, 2003, 2004 French National Institute For Research In Computer
 * Science And Control (INRIA).
 * Contact: jmob@objectweb.org
 * 
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation; either version 2.1 of the License, or any later
 * version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 * Initial developer(s): Emmanuel Cecchet, Julie Marguerite
 * Contributor(s): Jeremy Philippe, Niraj Tolia, Massimo Neri <hello@mneri.me>
 */

package edu.rice.rubis.client;

import fr.inria.jessy.Jessy;

import java.util.ArrayList;
import org.imdea.rubis.benchmark.stats.StatsCollector;

/**
 * RUBiS client emulator.
 * This class plays random user sessions emulating a Web browser.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 *         <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 *         <a href="mailto:hello@mneri.me">Massimo Neri</a>
 * @version 2.0
 */
public class ClientEmulator {
    private Jessy mJessy;
    private RUBiSProperties mProps;

    /**
     * Creates a new <code>ClientEmulator</code> instance.
     * The program is stopped on any error reading the configuration files.
     */
    public ClientEmulator(RUBiSProperties props, Jessy jessy) {
        mProps = props;
        mJessy = jessy;
    }

    public void start() {
        StatsCollector stats = new StatsCollector();

        ArrayList<UserSession> sessions = new ArrayList<>();
        stats.startEmulation();

        for (int i = 0; i < mProps.getNbOfClients(); i++) {
            UserSession session = new UserSession(mJessy, "UserSession" + i, mProps, stats);
            session.start();
            sessions.add(session);
        }

        for (int i = 0; i < mProps.getNbOfClients(); i++) {
            try {
                sessions.get(i).join();
            } catch (InterruptedException ie) {
                System.err.println("ClientEmulator: Thread " + i + " has been interrupted.");
            }
        }

        stats.endEmulation();
        stats.print();

        Runtime.getRuntime().exit(0);
    }
}
