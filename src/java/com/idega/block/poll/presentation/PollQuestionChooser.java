package com.idega.block.poll.presentation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

import com.idega.block.poll.business.PollBusiness;
import com.idega.core.accesscontrol.business.LoginBusinessBean;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.localisation.presentation.ICLocalePresentation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.Table;
import com.idega.presentation.text.Text;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;

public class PollQuestionChooser extends IWAdminWindow {

	private final static String IW_BUNDLE_IDENTIFIER = "com.idega.block.poll";
	private boolean isAdmin = false;
	private boolean superAdmin = false;
	private int userID = -1;
	public static String prmQuestions = "poll.questions";

	private IWResourceBundle iwrb;

	public PollQuestionChooser() {
		setWidth(400);
		setHeight(150);
	}

	public void main(IWContext iwc) throws Exception {
		/**
		 * @todo permission
		 */
		this.isAdmin = true; // AccessControl.hasEditPermission(this,iwc);
		this.superAdmin = iwc.isSuperAdmin();
		this.iwrb = getResourceBundle(iwc);
		addTitle(this.iwrb.getLocalizedString("poll_question_chooser", "Poll Question Chooser"));
		Locale currentLocale = iwc.getCurrentLocale();
		Locale chosenLocale;

		try {
			this.userID = LoginBusinessBean.getUser(iwc).getID();
		}
		catch (Exception e) {
			this.userID = -1;
		}

		String sLocaleId = iwc.getParameter(PollAdminWindow.prmLocale);

		int iLocaleId = -1;
		if (sLocaleId != null) {
			iLocaleId = Integer.parseInt(sLocaleId);
			chosenLocale = ICLocaleBusiness.getLocaleReturnIcelandicLocaleIfNotFound(iLocaleId);
		}
		else {
			chosenLocale = currentLocale;
			iLocaleId = ICLocaleBusiness.getLocaleId(chosenLocale);
		}

		if (this.isAdmin) {
			processForm(iwc, iLocaleId);
		}
		else {
			noAccess();
		}
	}

	private void processForm(IWContext iwc, int iLocaleId) {
		String pollQuestion = iwc.getParameter(prmQuestions);

		int pollQuestionID = -1;
		try {
			pollQuestionID = Integer.parseInt(pollQuestion);
		}
		catch (NumberFormatException e) {
			pollQuestionID = -1;
		}

		if (pollQuestionID != -1) {
			setToClose(iwc, pollQuestionID);
		}
		else {
			Form myForm = new Form();

			DropdownMenu localeDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(PollAdminWindow.prmLocale);
			localeDrop.setMarkupAttribute("style", STYLE);
			localeDrop.setToSubmit();
			localeDrop.setSelectedElement(Integer.toString(iLocaleId));

			DropdownMenu questionDrop = PollBusiness.getQuestions(prmQuestions, this.userID, iLocaleId, this.superAdmin);
			questionDrop.setMarkupAttribute("style", STYLE);
			questionDrop.setToSubmit();

			Text localeText = this.formatText(this.iwrb.getLocalizedString("locale", "Locale"), true);
			Text questionText = this.formatText(this.iwrb.getLocalizedString("questions", "Questions"), true);

			Table table = new Table(1, 2);
			table.setCellpadding(8);
			table.setWidth("100%");

			table.add(localeText, 1, 1);
			table.add(Text.getBreak(), 1, 1);
			table.add(localeDrop, 1, 1);

			table.add(questionText, 1, 2);
			table.add(Text.getBreak(), 1, 2);
			table.add(questionDrop, 1, 2);

			myForm.add(table);
			add(myForm);
		}
	}

	private void setToClose(IWContext iwc, int pollQuestionID) {
		iwc.setSessionAttribute(prmQuestions, Integer.toString(pollQuestionID));
		setParentToReload();
		close();
	}

	private void noAccess() throws IOException, SQLException {
		close();
	}

	public String getBundleIdentifier() {
		return IW_BUNDLE_IDENTIFIER;
	}

}
