/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.ut.biolab.medsavant.server.log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
 
public class BriefLogFormatter extends Formatter {
	
    private static final DateFormat format = new SimpleDateFormat("h:mm:ss");
    private static final String lineSep = System.getProperty("line.separator");

    /**
     * A Custom format implementation that is designed for brevity.
     */
    @Override
    public String format(LogRecord record) {
        String loggerName = record.getLoggerName();
        if(loggerName == null) {
                loggerName = "root";
        }
        StringBuilder output = new StringBuilder()
                .append(loggerName)
                .append("[")
                .append(record.getLevel()).append('|')
                .append(Thread.currentThread().getName()).append('|')
                .append(format.format(new Date(record.getMillis())))
                .append("]: ")
                .append(record.getMessage()).append(' ')
                .append(lineSep);
        return output.toString();		
    } 
}

