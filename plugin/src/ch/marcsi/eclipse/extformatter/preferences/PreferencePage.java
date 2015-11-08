package ch.marcsi.eclipse.extformatter.preferences;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;

import org.eclipse.jface.util.PropertyChangeEvent;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;

import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.marcsi.eclipse.extformatter.*;

import org.eclipse.ui.IWorkbench;
import org.eclipse.jface.resource.JFaceResources;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class PreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage, SelectionListener {

	static final String previewString = "namespace foospace \n" + "{ \n" + " int Foo()\n" + " {\n" + " if (isBar) { \n"
			+ " bar();\n" + " return 1;\n" + " } else \n" + " return 0;\n" + " }\n" + "}\n";

	StringFieldEditor cmdField;
	Button runButton;
	Document previewdocument;
	FontFieldEditor fontEditor;
	TextViewer textViewer;

	String previewCode = "";

	public PreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Settings for the CLI Formatter");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {
		Composite composite = getFieldEditorParent();

		cmdField = new StringFieldEditor(PreferenceConstants.COMMAND, "Command:", composite);
		addField(cmdField);

		Label label = new Label(composite, SWT.NONE);
		label.setText("Preview:");

		textViewer = new TextViewer(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		previewdocument = new Document(previewString);
		GridData gd = new GridData();
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = GridData.FILL;
		gd.verticalSpan = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;

		textViewer.getTextWidget().setLayoutData(gd);
		textViewer.getTextWidget().setFont(JFaceResources.getTextFont());
		textViewer.setDocument(previewdocument);

		runButton = new Button(composite, SWT.NONE);
		runButton.setText("run");
		runButton.addSelectionListener(this);

	}

	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
	}

	protected void checkState() {
		super.checkState();
	}

	public boolean performOk() {
		if (!updatePreview()) {
			return false;
		}
		return super.performOk();
	}

	/**
	 * @param updateReferenceStore
	 * @return
	 */
	private boolean updatePreview() {
		try {
			ExtFormatter formatter = new ExtFormatter();
			String formattedString;
			String cmd = cmdField.getStringValue();
			//Logger.logInfo("run with command: " + cmd);
			formattedString = formatter.formatString(cmd, previewString);

			//Logger.logInfo("preview: " + formattedString);
			previewdocument.set(formattedString);

			return true;

		} catch (CommandException e) {
			Logger.logError(e);
			setErrorMessage(e.getMessage());
			return false;
		}
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent arg0) {
	}

	@Override
	public void widgetSelected(SelectionEvent arg0) {
		updatePreview();
	}

}