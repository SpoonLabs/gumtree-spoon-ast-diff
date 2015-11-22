// {{{ QuickNotepad
/*
 * QuickNotepad.java
 * part of the QuickNotepad plugin for the jEdit text editor
 * Copyright (C) 2001 John Gellene
 * jgellene@nyc.rr.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * $Id: QuickNotepad.java,v 1.14 2006-07-06 13:30:42 vampire0 Exp $
 */

// {{{ imports
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.gjt.sp.jedit.EBComponent;
import org.gjt.sp.jedit.EBMessage;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.GUIUtilities;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.DefaultFocusComponent;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.msg.PropertiesChanged;
import org.gjt.sp.util.Log;
// }}}

// {{{ QuickNotePad class
/**
 * 
 * QuickNotePad - a dockable JPanel, a demonstration of a jEdit plugin.
 *
 */
public class QuickNotepad extends JPanel
    implements EBComponent, QuickNotepadActions, DefaultFocusComponent {

    // {{{ Instance Variables
	private static final long serialVersionUID = 6412255692894321789L;

	private String filename;

	private String defaultFilename;

	private View view;

	private boolean floating;

	private QuickNotepadTextArea textArea;

	private QuickNotepadToolPanel toolPanel;
    // }}}
    // {{{ Constructor
	/**
	 * 
	 * @param view the current jedit window
	 * @param position a variable passed in from the script in actions.xml,
	 * 	which can be DockableWindowManager.FLOATING, TOP, BOTTOM, LEFT, RIGHT, etc.
	 * 	see @ref DockableWindowManager for possible values.
	 */
	public QuickNotepad(View view, String position) {
		super(new BorderLayout());
		this.view = view;
		this.floating = position.equals(DockableWindowManager.FLOATING);

		if (jEdit.getSettingsDirectory() != null) {
			this.filename = jEdit.getProperty(QuickNotepadPlugin.OPTION_PREFIX
					+ "filepath");
			if (this.filename == null || this.filename.length() == 0) {
				this.filename = new String(jEdit.getSettingsDirectory()
						+ File.separator + "qn.txt");
				jEdit.setProperty(
						QuickNotepadPlugin.OPTION_PREFIX + "filepath",
						this.filename);
			}
			this.defaultFilename = this.filename;
		}

		this.toolPanel = new QuickNotepadToolPanel(this);
		add(BorderLayout.NORTH, this.toolPanel);

		if (floating)
			this.setPreferredSize(new Dimension(500, 250));

		textArea = new QuickNotepadTextArea();
		textArea.setFont(QuickNotepadOptionPane.makeFont());
		if(true){
			System.out.println("test");
		}
		
		JScrollPane pane = new JScrollPane(textArea);
		add(BorderLayout.CENTER, pane);

		readFile();
	}
    // }}}

    // {{{ Member Functions
    
    // {{{ focusOnDefaultComponent
	public void focusOnDefaultComponent() {
		textArea.requestFocus();
	}
    // }}}

    // {{{ getFileName
	public String getFilename() {
		return filename;
	}
    // }}}

	// EBComponent implementation
	
    // {{{ handleMessage
	public void handleMessage(EBMessage message) {
		if (message instanceof PropertiesChanged) {
			propertiesChanged();
		}
	}
    // }}}
    
    // {{{ propertiesChanged
	private void propertiesChanged() {
		String propertyFilename = jEdit
				.getProperty(QuickNotepadPlugin.OPTION_PREFIX + "filepath");
		if (!MiscUtilities.objectsEqual(defaultFilename, propertyFilename)) {
			saveFile();
			toolPanel.propertiesChanged();
			defaultFilename = propertyFilename;
			filename = defaultFilename;
			readFile();
		}
		Font newFont = QuickNotepadOptionPane.makeFont();
		if (!newFont.equals(textArea.getFont())) {
			textArea.setFont(newFont);
		}
	}
    // }}}

	// These JComponent methods provide the appropriate points
	// to subscribe and unsubscribe this object to the EditBus.

    // {{{ addNotify
	public void addNotify() {
		super.addNotify();
		EditBus.addToBus(this);
	}
     // }}}
     
    // {{{ removeNotify
	public void removeNotify() {
		saveFile();
		super.removeNotify();
		EditBus.removeFromBus(this);
	}
    // }}}
    
	// QuickNotepadActions implementation

    // {{{
	public void saveFile() {
		if (filename == null || filename.length() == 0)
			return;
		try {
			FileWriter out = new FileWriter(filename);
			out.write(textArea.getText());
			out.close();
		} catch (IOException ioe) {
			Log.log(Log.ERROR, QuickNotepad.class,
					"Could not write notepad text to " + filename);
		}
	}
    // }}}
    
    // {{{ chooseFile
	public void chooseFile() {
		String[] paths = GUIUtilities.showVFSFileDialog(view, null,
				JFileChooser.OPEN_DIALOG, false);
		if (paths != null && !paths[0].equals(filename)) {
			saveFile();
			filename = paths[0];
			toolPanel.propertiesChanged();
			readFile();
		}
	}
    // }}}

    // {{{ copyToBuffer
	public void copyToBuffer() {
		jEdit.newFile(view);
		view.getEditPane().getTextArea().setText(textArea.getText());
	}
    // }}}
    // {{{ readFile()
	/**
	 * Helper method
	 */
	private void readFile() {
		if (filename == null || filename.length() == 0)
			return;

		BufferedReader bf = null;
		try {
			bf = new BufferedReader(new FileReader(filename));
			StringBuffer sb = new StringBuffer(2048);
			String str;
			while ((str = bf.readLine()) != null) {
				sb.append(str).append('\n');
			}
			bf.close();
			textArea.setText(sb.toString());
		} catch (FileNotFoundException fnf) {
			Log.log(Log.ERROR, QuickNotepad.class, "notepad file " + filename
					+ " does not exist");
		} catch (IOException ioe) {
			Log.log(Log.ERROR, QuickNotepad.class,
					"could not read notepad file " + filename);
		}
	}
    // }}}
    // }}}
}
// }}}
