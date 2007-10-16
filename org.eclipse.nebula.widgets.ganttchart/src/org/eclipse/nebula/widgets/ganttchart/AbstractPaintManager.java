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

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

public abstract class AbstractPaintManager implements IPaintManager {
	
	public void drawEvent(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, boolean isSelected, boolean threeDee, int dayWidth, int x, int y, int eventWidth) {
		// draw the border
		gc.setForeground(colorManager.getEventBorderColor());
		if (isSelected) {
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawRectangle(x, y, eventWidth, settings.getEventHeight());
			gc.setLineStyle(SWT.LINE_SOLID);
		}
		else {
			gc.drawRectangle(x, y, eventWidth, settings.getEventHeight());
		}
		
		Color cEvent = ge.getStatusColor();
		Color gradient = ge.getGradientStatusColor();

		if (cEvent == null)
			cEvent = settings.getDefaultEventColor();
		if (gradient == null)
			gradient = settings.getDefaultGradientEventColor();
		
		// draw the insides
		gc.setBackground(cEvent);

		if (settings.showGradientBars()) {
			gc.setForeground(gradient);
			gc.fillGradientRectangle(x + 1, y + 1, eventWidth - 1, settings.getEventHeight() - 1, true);
			gc.setForeground(colorManager.getEventBorderColor()); // re-set foreground color
		}
		else {
			gc.fillRectangle(x + 1, y + 1, eventWidth - 1, settings.getEventHeight() - 1);
		}

		// if 3D effect, draw drop-shadow
		if (threeDee) {
			gc.setForeground(colorManager.getFadeOffColor1());
			// horizontal line.. ends a few pixles right of bottom right corner
			gc.drawLine(x, y + settings.getEventHeight() + 1, x + eventWidth + 1, y + settings.getEventHeight() + 1);
			// vertical line at end, starts slightly below top right corner
			gc.drawLine(x + eventWidth + 1, y + 2, x + eventWidth + 1, y + settings.getEventHeight());

			gc.setForeground(colorManager.getFadeOffColor2());
			gc.drawLine(x, y + settings.getEventHeight() + 2, x + eventWidth + 1, y + settings.getEventHeight() + 2); // h

			gc.setForeground(colorManager.getFadeOffColor1());
			gc.drawLine(x, y + settings.getEventHeight() + 3, x + eventWidth + 1, y + settings.getEventHeight() + 3); // h
			// next vertical starts 1 pixel further down and 1 pixel further right and dips 1 pixel below bottom
			gc.drawLine(x + eventWidth + 2, y + 3, x + eventWidth + 2, y + settings.getEventHeight() + 1); // v
		}

		if (ge.getPercentComplete() > 0 && ge.getPercentComplete() <= 100) {
			int yStart = y + (settings.getEventHeight() / 2) - 1;

			// xEnd is how long the event box is
			// how much of that in % are we showing?
			float perc = (float) ge.getPercentComplete() / 100f;
			// and how many pixels is that?
			float toDraw = (float) eventWidth * perc;

			// draw the inner bar
			gc.setBackground(colorManager.getPercentageBarColor());
			gc.fillRectangle(x, yStart - settings.getEventPercentageBarHeight()/2 + 1, (int) toDraw, settings.getEventPercentageBarHeight());
		}
	}

	public void drawCheckpoint(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, boolean threeDee, int dayWidth, int x, int y) {
		Color cEvent = ge.getStatusColor();
		
		if (cEvent == null)
			cEvent = settings.getDefaultEventColor();
		
		gc.setBackground(cEvent);
		
		int height = settings.getEventHeight();

		// draw a special fun thing! (tm)
		long days = DateHelper.daysBetween(ge.getStartDate().getTime(), ge.getEndDate().getTime());
	
		drawCheckpointMarker(gc, settings, colorManager, ge, threeDee, x, y, dayWidth, height);
		if (days != 0) {
			int width = (int)days * dayWidth;
			drawCheckpointMarker(gc, settings, colorManager, ge, threeDee, x+width, y, dayWidth, height);
			
			// draw center
			int neg = height / 2 - 1;
			Rectangle rect = new Rectangle(x + dayWidth, y + neg, width - dayWidth, neg);
			gc.setForeground(colorManager.getBlack());
			gc.fillRectangle(rect);
			gc.drawRectangle(rect);
			
			if (settings.showBarsIn3D()) {
				gc.setForeground(colorManager.getFadeOffColor1());
				gc.drawLine(rect.x + 1, rect.y + rect.height + 1, rect.x + rect.width - 1, rect.y + rect.height + 1);
				gc.setForeground(colorManager.getFadeOffColor2());
				gc.drawLine(rect.x + 1, rect.y + rect.height + 2, rect.x + rect.width - 1, rect.y + rect.height + 2);
				gc.setForeground(colorManager.getFadeOffColor3());
				gc.drawLine(rect.x + 1, rect.y + rect.height + 3, rect.x + rect.width - 1, rect.y + rect.height + 3);
			}
		}
	}
	
	private void drawCheckpointMarker(GC gc, ISettings settings, IColorManager colorManager, GanttEvent ge, boolean threeDee, int x, int y, int width, int height) {
		float fHoriSpacer = (float)width * 0.17f;
		int hSpacer = (int)fHoriSpacer;
		
		float fVertiSpacer = (float)height * 0.23f;
		int vSpacer = (int)fVertiSpacer;
		
		Rectangle topToBottom = new Rectangle(x+hSpacer, y, width-(hSpacer*2), height+vSpacer);
		Rectangle leftToRight = new Rectangle(x, y+vSpacer, width, height-vSpacer);
		Rectangle inner = new Rectangle(x+hSpacer, y+vSpacer, width-(hSpacer*2), height-(vSpacer*2));

		Color cEvent = ge.getStatusColor();
		Color gradient = ge.getGradientStatusColor();

		if (cEvent == null)
			cEvent = settings.getDefaultEventColor();
		if (gradient == null)
			gradient = settings.getDefaultGradientEventColor();
		
		gc.setForeground(gradient);
		gc.setBackground(cEvent);
		gc.fillRectangle(topToBottom);
		gc.fillRectangle(leftToRight);		
		gc.fillGradientRectangle(inner.x, inner.y, inner.width, inner.height, true);
		
		gc.setForeground(colorManager.getBlack());		
		
		gc.drawRectangle(topToBottom);		
		gc.drawRectangle(leftToRight);
		
		if (threeDee) {
			gc.setForeground(colorManager.getFadeOffColor1());
			// horizontal line.. ends a few pixles right of bottom right corner
			gc.drawLine(leftToRight.x, leftToRight.y + leftToRight.height + 1, leftToRight.x + hSpacer - 1, leftToRight.y + leftToRight.height + 1);
			gc.drawLine(leftToRight.x + hSpacer, leftToRight.y + leftToRight.height + vSpacer + 1, leftToRight.x - hSpacer + leftToRight.width, leftToRight.y + leftToRight.height + vSpacer + 1);
			gc.drawLine(leftToRight.x + leftToRight.width - hSpacer + 1, leftToRight.y + leftToRight.height + 1, leftToRight.x + leftToRight.width + 1, leftToRight.y + leftToRight.height + 1);
			
			// vertical line at end, starts slightly below top right corner
			gc.drawLine(leftToRight.x + leftToRight.width + 1, leftToRight.y + 2, leftToRight.x + leftToRight.width + 1, leftToRight.y + leftToRight.height + 1);

			gc.setForeground(colorManager.getFadeOffColor2());
			gc.drawLine(leftToRight.x, leftToRight.y + leftToRight.height + 2, leftToRight.x + hSpacer - 1, leftToRight.y + leftToRight.height + 2);
			gc.drawLine(leftToRight.x + hSpacer, leftToRight.y + leftToRight.height + vSpacer + 2, leftToRight.x - hSpacer + leftToRight.width, leftToRight.y + leftToRight.height + vSpacer + 2);
			gc.drawLine(leftToRight.x + leftToRight.width - hSpacer + 1, leftToRight.y + leftToRight.height + 2, leftToRight.x + leftToRight.width + 1, leftToRight.y + leftToRight.height + 2);
			
			gc.drawLine(leftToRight.x + leftToRight.width + 2, leftToRight.y + 3, leftToRight.x + leftToRight.width + 2, leftToRight.y + leftToRight.height + 1);
			
			gc.setForeground(colorManager.getFadeOffColor3());
			gc.drawLine(leftToRight.x, leftToRight.y + leftToRight.height + 3, leftToRight.x + hSpacer - 1, leftToRight.y + leftToRight.height + 3);
			gc.drawLine(leftToRight.x + hSpacer, leftToRight.y + leftToRight.height + vSpacer + 3, leftToRight.x - hSpacer + leftToRight.width, leftToRight.y + leftToRight.height + vSpacer + 3);
			gc.drawLine(leftToRight.x + leftToRight.width - hSpacer + 1, leftToRight.y + leftToRight.height + 3, leftToRight.x + leftToRight.width + 1, leftToRight.y + leftToRight.height + 3);
			
			gc.drawLine(leftToRight.x + leftToRight.width + 3, leftToRight.y + 4, leftToRight.x + leftToRight.width + 3, leftToRight.y + leftToRight.height + 1);
		}
				
		
	}

	public void drawRevisedDates(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, boolean threeDee, int x, int y, int eventWidth) {	
		
		int spacer = settings.getRevisedLineSpacer();
		
		if (ge.getRevisedStart() != null && !ge.getRevisedStart().equals(ge.getStartDate())) {
			int xe = ganttComposite.getStartingXfor(ge.getRevisedStart());
			int ys = y - spacer;
			gc.setForeground(colorManager.getRevisedStartColor());
			gc.drawLine(xe, ys, x, ys);
			gc.drawLine(xe, ys - 3, xe, ys + 3);
			gc.drawLine(x, ys - 3, x, ys + 3);
		}
		if (ge.getRevisedEnd() != null && !ge.getRevisedEnd().equals(ge.getEndDate())) {
			int xe = ganttComposite.getStartingXfor(ge.getRevisedEnd());
			int ys = y + settings.getEventHeight() + spacer;
			gc.setForeground(colorManager.getRevisedEndColor());
			gc.drawLine(xe, ys, x + eventWidth, ys);
			gc.drawLine(xe, ys - 3, xe, ys + 3);
			gc.drawLine(x + eventWidth, ys - 3, x + eventWidth, ys + 3);
		}
	}

	public void drawDaysOnChart(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, boolean threeDee, int x, int y, int eventWidth, int daysNumber) {
		if (ge.isImage())
			return;
		
		int top = y - 2;
		int xE = x + eventWidth;
		int middle = x + ((xE - x) / 2);
		int yMiddle = ge.getY() + (ge.getHeight()/2);

		Point extent = gc.stringExtent(""+daysNumber);
		extent.x = extent.x + (2 * 2); // 2 pixel spacing on 2 sides, for clarities sake
		
		Color gradient = ge.getGradientStatusColor();

		if (gradient == null)
			gradient = settings.getDefaultGradientEventColor();
		
		if ((middle - extent.x) > x) {
			gc.setBackground(gradient);
			gc.fillRectangle(middle - extent.x/2, top, extent.x, settings.getEventHeight() + 4);
			gc.setForeground(colorManager.getTextColor());
			gc.drawRectangle(middle - extent.x/2, top, extent.x, settings.getEventHeight() + 4);

			String strDays = "" + daysNumber;
			Point dx = gc.stringExtent(strDays);
			yMiddle -= dx.y/2;
			gc.drawString(strDays, middle - dx.x + (dx.x / 2) + 1, yMiddle, true);
		}
	}

	public void drawEventString(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, String toDraw, boolean threeDee, int x, int y, int eventWidth) {
		int textEndX = 0;
		int yTextPos = y + (ge.getHeight()/2);
		
		Font oldFont = gc.getFont();
		
		gc.setForeground(colorManager.getTextColor());
		if (ge.showBoldText()) {
			FontData[] old = oldFont.getFontData();
			old[0].setStyle(SWT.BOLD);
			Font f = new Font(Display.getDefault(), old);
			gc.setFont(f);
			// MUST DISPOSE FONT or we'll run out of handles (win XP)
			f.dispose();
		}
		
		Point toDrawExtent = gc.stringExtent(toDraw);
		yTextPos -= (toDrawExtent.y/2)-1;

		gc.drawString(toDraw, x + eventWidth + settings.getTextSpacer(), yTextPos, true);
		textEndX = x + eventWidth + settings.getTextSpacer() + gc.stringExtent(toDraw).x;
		
		Image lockImage = settings.getLockImage();
		
		// draw lock icon if parent phase is locked
		if (ge.isLocked() && textEndX != 0 && lockImage != null) {
			gc.drawImage(lockImage, textEndX, y);
		}
	}

	public void drawScope(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, boolean threeDee, int dayWidth, int x, int y, int eventWidth) {
		List<GanttEvent> scopeEvents = ge.getScopeEvents();
		
		// empty scope
		if (scopeEvents.size() == 0)
			return;
		
		gc.setBackground(colorManager.getScopeGradientColorBottom());
		gc.setForeground(colorManager.getScopeGradientColorTop());
		
		gc.fillGradientRectangle(x, y, eventWidth, settings.getEventHeight()-5, true);
		gc.setForeground(colorManager.getScopeBorderColor());
		gc.drawRectangle(new Rectangle(x, y, eventWidth, settings.getEventHeight()-5));
		
		gc.setBackground(colorManager.getScopeGradientColorBottom());
		gc.setForeground(colorManager.getScopeGradientColorTop());
		gc.fillGradientRectangle(x, y, dayWidth/2, settings.getEventHeight(), true);
		gc.fillGradientRectangle(x+eventWidth+(dayWidth/2)-dayWidth, y, dayWidth/2, settings.getEventHeight(), true);
		gc.setForeground(colorManager.getScopeBorderColor());
		gc.drawRectangle(x, y, dayWidth/2, settings.getEventHeight());
		gc.drawRectangle(x+eventWidth+(dayWidth/2)-dayWidth, y, dayWidth/2, settings.getEventHeight());
		
		if (threeDee) {
			gc.setForeground(colorManager.getFadeOffColor1());
			gc.drawLine(x, y+settings.getEventHeight()+1, x+dayWidth/2, y+settings.getEventHeight()+1);
			gc.drawLine(x+eventWidth-(dayWidth/2), y+settings.getEventHeight()+1, x+eventWidth, y+settings.getEventHeight()+1);
			gc.drawLine(x+(dayWidth/2)+1, y+settings.getEventHeight()-4, x+eventWidth-(dayWidth/2)-1, y+settings.getEventHeight()-4);
			
			gc.setForeground(colorManager.getFadeOffColor2());
			gc.drawLine(x, y+settings.getEventHeight()+2, x+dayWidth/2, y+settings.getEventHeight()+2);
			gc.drawLine(x+eventWidth-(dayWidth/2), y+settings.getEventHeight()+2, x+eventWidth, y+settings.getEventHeight()+2);
			gc.drawLine(x+(dayWidth/2)+1, y+settings.getEventHeight()-3, x+eventWidth-(dayWidth/2)-1, y+settings.getEventHeight()-3);
			
			gc.setForeground(colorManager.getFadeOffColor3());
			gc.drawLine(x, y+settings.getEventHeight()+3, x+dayWidth/2, y+settings.getEventHeight()+3);
			gc.drawLine(x+eventWidth-(dayWidth/2), y+settings.getEventHeight()+3, x+eventWidth, y+settings.getEventHeight()+3);
			gc.drawLine(x+(dayWidth/2)+1, y+settings.getEventHeight()-2, x+eventWidth-(dayWidth/2)-1, y+settings.getEventHeight()-2);
		}
		
	}

	public void drawImage(GanttComposite ganttComposite, ISettings settings, IColorManager colorManager, GanttEvent ge, GC gc, Image image, boolean threeDee, int dayWidth, int x, int y) {
		
		// draw a cross in a box if image is null
		if (image == null) {
			gc.setForeground(colorManager.getBlack());
			gc.drawRectangle(x, y, dayWidth, settings.getEventHeight());
			gc.drawLine(x, y, x+dayWidth, y+settings.getEventHeight());
			gc.drawLine(x+dayWidth, y, x, y+settings.getEventHeight());
			return;
		}
		
		// can it fit?
		Rectangle bounds = image.getBounds();
		if (bounds.width > dayWidth) {
			// shrink image
			ImageData id = image.getImageData();
			int diff = id.width-dayWidth;
			id.width -= diff;
			id.height -= diff;
			Image temp = new Image(Display.getDefault(), id);
			
			int negY = (bounds.height - settings.getEventHeight());
			if (negY > 0) 
				y += negY/2;
			
			gc.drawImage(temp, x, y);
			temp.dispose();
			return;
		}
		else {
			// center it x-wise
			x -= (bounds.width - dayWidth) / 2;
		}
		
		gc.drawImage(image, x, y);
	}
	
	
}
