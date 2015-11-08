package ch.marcsi.eclipse.extformatter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.text.edits.*;

import ch.marcsi.eclipse.extformatter.preferences.PreferenceConstants;

import org.apache.commons.io.IOUtils;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;


public class ExtFormatter extends CodeFormatter {
	private diff_match_patch dmp = new diff_match_patch();
	
	
	public ExtFormatter() {
		dmp.Diff_Timeout = 0.1f; // max 100ms
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.core.org.eclipse.cdt.indent.CodeFormatter#format(int,
	 * org.eclipse.jface.text.IDocument, int, int, int, java.lang.String)
	 */
	public TextEdit format(int kind, String source, int offset, int length, int indentationLevel,
			String lineSeparator) {
		String ssource = source.substring(offset, offset + length);
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String target;
		
		try {
			String cmd = store.getString(PreferenceConstants.COMMAND);
			target = formatString(cmd, ssource);
		} catch (CommandException e) {
			return null;
		}
		
		boolean ad = store.getBoolean(PreferenceConstants.DIFF);
		if (!ad) {
			return new ReplaceEdit(offset,length,target);
		}
		
		LinkedList<Diff> diffs = dmp.diff_main(ssource,target);
		MultiTextEdit edits = new MultiTextEdit();
		
		int loc = offset;
		for (Diff d : diffs) {
			//Logger.logInfo(d.toString());
			if (d.operation == Operation.EQUAL) {
				loc += d.text.length();
			} else if (d.operation == Operation.DELETE) {
				edits.addChild(new DeleteEdit(loc, d.text.length()));
				loc += d.text.length();
			} else if (d.operation == Operation.INSERT) {
				edits.addChild(new InsertEdit(loc, d.text));
			}
		}
		return edits;
	}

	public String formatString(String cmd, String source) throws CommandException {
		try {
			String[] cmds = cmd.split(Pattern.quote(" "));
			
			ProcessBuilder pb = new ProcessBuilder(cmds);
			Process p = pb.start();
			PrintWriter out = new PrintWriter(p.getOutputStream());
			out.write(source);
			out.flush();
			out.close();
			int r = p.waitFor();
			
			StringWriter in = new StringWriter();
			IOUtils.copy(p.getInputStream(), in);
			String target = in.toString();
			
			if (r != 0) {
				StringWriter err = new StringWriter();
				IOUtils.copy(p.getErrorStream(), err);
				String error = err.toString();
				
				throw new CommandException(error);
			}

			return target;
		} catch (IOException e) {
			Logger.logError(e);
			throw new CommandException(e.getMessage());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	public boolean supportProperty(String propertyID) {
		return false;
	}

	public String[][] getEnumerationProperty(String propertyID) {
		String[][] empty = { { "" } };
		return empty;
	}

	@Override
	public void setOptions(Map<String, ?> options) {
		
	}
}
