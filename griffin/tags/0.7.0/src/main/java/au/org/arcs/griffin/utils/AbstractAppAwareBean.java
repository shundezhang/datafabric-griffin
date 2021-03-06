/*
 * ------------------------------------------------------------------------------
 * Hermes FTP Server
 * Copyright (c) 2005-2007 Lars Behnke
 * ------------------------------------------------------------------------------
 * 
 * This file is part of Hermes FTP Server.
 * 
 * Hermes FTP Server is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * Hermes FTP Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Hermes FTP Server; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * ------------------------------------------------------------------------------
 */

package au.org.arcs.griffin.utils;


import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import au.org.arcs.griffin.common.BeanConstants;

/**
 * Provides a basic, application context aware class that can be subclassed.
 * 
 * @author Lars Behnke
 */
public abstract class AbstractAppAwareBean implements ApplicationContextAware, BeanConstants {

    private ApplicationContext applicationContext;

    /**
     * {@inheritDoc}
     */
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;

    }

    /**
     * Getter method for the java bean <code>applicationContext</code>.
     * 
     * @return Returns the value of the java bea <code>applicationContext</code>.
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;

    }

}
