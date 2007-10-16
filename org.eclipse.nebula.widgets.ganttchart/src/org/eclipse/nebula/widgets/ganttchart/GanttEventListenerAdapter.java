/*******************************************************************************
 * Copyright (c) Emil Crumhorn - Hexapixel.com - emil.crumhorn@gmail.com
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    emil.crumhorn@gmail.com - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.nebula.widgets.ganttchart;

import java.util.List;

import org.eclipse.swt.events.MouseEvent;

public class GanttEventListenerAdapter implements IGanttEventListener {

	public void eventDoubleClicked(GanttEvent event, MouseEvent me) {
	}

	public void eventPropertiesSelected(GanttEvent event) {
	}

	public void eventsDeleteRequest(List<GanttEvent> events, MouseEvent me) {
	}

	public void eventSelected(GanttEvent event, List<GanttEvent> allSelectedEvents, MouseEvent me) {
	}

	public void eventsMoved(List<GanttEvent> events, MouseEvent me) {
	}

	public void eventsResized(List<GanttEvent> events, MouseEvent me) {
	}

	public void zoomedIn(int newZoomLevel) {
	}

	public void zoomedOut(int newZoomLevel) {
	}

	public void zoomReset() {
	}

}
