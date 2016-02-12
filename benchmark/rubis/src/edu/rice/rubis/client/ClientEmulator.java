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

/**
 * RUBiS client emulator.
 * This class plays random user sessions emulating a Web browser.
 *
 * @author <a href="mailto:cecchet@rice.edu">Emmanuel Cecchet</a> and
 *         <a href="mailto:julie.marguerite@inrialpes.fr">Julie Marguerite</a>
 * @version 1.0
 */
public class ClientEmulator {
    private static boolean endOfSimulation = false;
    private Jessy mJessy;
    private RUBiSProperties rubis = null;

    /**
     * Creates a new <code>ClientEmulator</code> instance.
     * The program is stopped on any error reading the configuration files.
     */
    public ClientEmulator(Jessy jessy) {
        mJessy = jessy;
        rubis = new RUBiSProperties();
        TransitionTable transition = new TransitionTable(rubis.getNbOfColumns(), rubis.getNbOfRows());

        if (!transition.readExcelTextFile(rubis.getTransitionTable()))
            Runtime.getRuntime().exit(1);
        else
            transition.displayMatrix();
    }

    /**
     * True if end of simulation has been reached.
     *
     * @return true if end of simulation
     */
    public static synchronized boolean isEndOfSimulation() {
        return endOfSimulation;
    }

    /**
     * Set the end of the current simulation
     */
    private synchronized void setEndOfSimulation() {
        endOfSimulation = true;
    }

    public void start() {
        rubis.checkPropertiesFileAndGetURLGenerator();

        System.out.println("Initializing DB...");
        InitDB init = new InitDB(mJessy);
        init.generateUsers();
        init.generateItems();
        System.out.println("DB initialized...");

        UserSession[] sessions = new UserSession[rubis.getNbOfClients()];

        for (int i = 0; i < rubis.getNbOfClients(); i++) {
            sessions[i] = new UserSession(mJessy, "UserSession" + i, rubis);
            sessions[i].start();
        }

        setEndOfSimulation();

        for (int i = 0; i < rubis.getNbOfClients(); i++) {
            try {
                sessions[i].join(2000);
            } catch (InterruptedException ie) {
                System.err.println("ClientEmulator: Thread " + i + " has been interrupted.");
            }
        }

        Runtime.getRuntime().exit(0);
    }
}
