package com.idega.block.poll.presentation;


import java.sql.*;
import java.util.*;
import java.io.*;
import com.idega.util.*;
import com.idega.presentation.text.*;
import com.idega.presentation.*;
import com.idega.presentation.ui.*;
import com.idega.core.localisation.presentation.ICLocalePresentation;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.data.ICLocale;
import com.idega.block.poll.data.*;
import com.idega.block.poll.business.*;
import com.idega.core.accesscontrol.business.AccessControl;
import com.idega.block.login.business.LoginBusiness;
import com.idega.block.text.business.TextFinder;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;

public class PollQuestionChooser extends IWAdminWindow{

private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.poll";
private boolean isAdmin = false;
private boolean superAdmin = false;
private boolean close = false;
private int pollID = -1;
private int userID = -1;
public static String prmQuestions = "poll.questions";

private IWBundle iwb;
private IWResourceBundle iwrb;

public PollQuestionChooser(){
  setWidth(400);
  setHeight(150);
}

  public void main(IWContext iwc) throws Exception {
    /**
     * @todo permission
     */
    isAdmin = true; //AccessControl.hasEditPermission(this,iwc);
    superAdmin = iwc.isSuperAdmin();
    iwb = getBundle(iwc);
    iwrb = getResourceBundle(iwc);
    addTitle(iwrb.getLocalizedString("poll_question_chooser","Poll Question Chooser"));
    Locale currentLocale = iwc.getCurrentLocale(),chosenLocale;

    try {
      userID = LoginBusiness.getUser(iwc).getID();
    }
    catch (Exception e) {
      userID = -1;
    }

    String sLocaleId = iwc.getParameter(PollAdminWindow.prmLocale);

    int iLocaleId = -1;
    if(sLocaleId!= null){
      iLocaleId = Integer.parseInt(sLocaleId);
      chosenLocale = TextFinder.getLocale(iLocaleId);
    }
    else{
      chosenLocale = currentLocale;
      iLocaleId = ICLocaleBusiness.getLocaleId(chosenLocale);
    }

    if ( isAdmin ) {
      processForm(iwc, iLocaleId);
    }
    else {
      noAccess();
    }
  }

  private void processForm(IWContext iwc, int iLocaleId) {
    String pollQuestion = iwc.getParameter(prmQuestions);
    String pollIDString = iwc.getParameter(Poll._prmPollID);
    try {
      pollID = Integer.parseInt(pollIDString);
    }
    catch (NumberFormatException e) {
      pollID = -1;
    }

    int pollQuestionID = -1;
    try {
      pollQuestionID = Integer.parseInt(pollQuestion);
    }
    catch (NumberFormatException e) {
      pollQuestionID = -1;
    }

    if ( pollQuestionID != -1 ) {
      setToClose(iwc, pollQuestionID);
    }
    else {
      Form myForm = new Form();

      DropdownMenu localeDrop = ICLocalePresentation.getLocaleDropdownIdKeyed(PollAdminWindow.prmLocale);
        localeDrop.setAttribute("style",STYLE);
        localeDrop.setToSubmit();
        localeDrop.setSelectedElement(Integer.toString(iLocaleId));

      DropdownMenu questionDrop = PollBusiness.getQuestions(prmQuestions,userID,iLocaleId,superAdmin);
        questionDrop.setAttribute("style",STYLE);
        questionDrop.setToSubmit();

      Text localeText = this.formatText(iwrb.getLocalizedString("locale","Locale"),true);
      Text questionText = this.formatText(iwrb.getLocalizedString("questions","Questions"),true);

      Table table = new Table(1,2);
        table.setCellpadding(8);
        table.setAlignment("center");
        table.setWidth("100%");

      table.add(localeText,1,1);
      table.add(Text.getBreak(),1,1);
      table.add(localeDrop,1,1);

      table.add(questionText,1,2);
      table.add(Text.getBreak(),1,2);
      table.add(questionDrop,1,2);

      myForm.add(table);
      add(myForm);
    }
  }

  private void setToClose(IWContext iwc, int pollQuestionID) {
    iwc.setSessionAttribute(prmQuestions,Integer.toString(pollQuestionID));
    setParentToReload();
    close();
  }

  private void noAccess() throws IOException,SQLException {
    close();
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

}
