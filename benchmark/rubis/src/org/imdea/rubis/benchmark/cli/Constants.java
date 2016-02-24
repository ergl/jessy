/*
 * RUBiS Benchmark
 * Copyright (C) 2016 IMDEA Software Institute
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
 */

package org.imdea.rubis.benchmark.cli;

class Constants {
    static final String OPT_CLIENT = "c";
    static final String OPT_CLIENT_DESC = "Launch a RUBiS client.";
    static final String OPT_CLIENT_LONG = "client";

    static final String OPT_HELP = "h";
    static final String OPT_HELP_DESC = "Print this message.";
    static final String OPT_HELP_LONG = "help";

    static final String OPT_PROPERTIES = "p";
    static final String OPT_PROPERTIES_DEFAULT = "./rubis.properties";
    static final String OPT_PROPERTIES_DESC = "The properties file.";
    static final String OPT_PROPERTIES_LONG = "properties";

    static final String OPT_SERVER = "s";
    static final String OPT_SERVER_DESC = "Launch a RUBiS server.";
    static final String OPT_SERVER_LONG = "server";
}
