package com.idega.block.poll.presentation;

import com.idega.jmodule.object.interfaceobject.*;
import com.idega.jmodule.object.ModuleInfo;
import com.idega.jmodule.object.*;
import com.idega.jmodule.object.textObject.Text;
import com.idega.block.poll.business.*;
import com.idega.block.poll.data.*;
import com.idega.block.text.business.TextFinder;
import com.idega.block.text.data.LocalizedText;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.core.localisation.business.ICLocaleBusiness;
import com.idega.core.accesscontrol.business.AccessControl;
import java.sql.SQLException;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */

public class PollResult extends Window {

private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.poll";
protected IWResourceBundle _iwrb;
protected IWBundle _iwb;

private boolean _isAdmin;
private boolean _showCollection = false;
private boolean _save = false;
private boolean _showVotes = true;
private int _numberOfPolls = 3;
private int _pollQuestionID = -1;
private int _iLocaleID;
private Image line;

Table layoutTable;

public PollResult() {
  setWidth(280);
  setHeight(230);
  setResizable(true);
  setScrollbar(false);
}

  public void main(ModuleInfo modinfo) throws Exception {
    _iwrb = getResourceBundle(modinfo);
    _iwb = getBundle(modinfo);

    setAllMargins(0);
    setTitle(_iwrb.getLocalizedString("results","Results"));

    _isAdmin = AccessControl.hasEditPermission(this,modinfo);
    _iLocaleID = ICLocaleBusiness.getLocaleId(modinfo.getCurrentLocale());

    String collectionString = modinfo.getParameter(Poll._prmPollCollection);
    String showVotesString = modinfo.getParameter(Poll._prmShowVotes);
    String numberOfPollsString = modinfo.getParameter(Poll._prmNumberOfPolls);

    try {
      _pollQuestionID = Integer.parseInt(modinfo.getParameter(PollBusiness._PARAMETER_POLL_QUESTION));
    }
    catch (NumberFormatException e) {
      _pollQuestionID = -1;
    }

    drawLayout();

    if ( collectionString != null ) {
      _showCollection = true;
    }
    if ( showVotesString != null ) {
      _showVotes = true;
    }
    if ( numberOfPollsString != null ) {
      try {
        _numberOfPolls = Integer.parseInt(numberOfPollsString);
      }
      catch (NumberFormatException e) {
        _numberOfPolls = 3;
      }
    }

    if ( _showCollection ) {
      showCollection();
    }
    else {
      if (PollBusiness.thisObjectSubmitted(modinfo.getParameter(PollBusiness._PARAMETER_POLL_VOTER))){
        System.out.println("ObjectSubmitted");
        PollBusiness.handleInsert(modinfo);
      }

      if ( _pollQuestionID != -1 )
        layoutTable.add(showResults(_pollQuestionID),1,3);
      else
        this.getParentPage().close();
    }
  }

  private void drawLayout() {
    layoutTable = new Table(1,5);
    layoutTable.setWidth("100%");
    layoutTable.setCellpadding(0);
    layoutTable.setCellspacing(0);
    layoutTable.setHeight(2,"9");
    layoutTable.setHeight(4,"17");
    layoutTable.setAlignment(1,5,"center");

    Image header = _iwrb.getImage("top.gif");
    line = _iwb.getImage("/shared/line.gif");
    CloseButton close = new CloseButton(_iwrb.getImage("close.gif"));

    layoutTable.add(header,1,1);
    layoutTable.add(line,1,2);
    layoutTable.add(line,1,4);
    layoutTable.add(close,1,5);

    add(layoutTable);
  }

  private Table showResults(int pollQuestionID) {
    Table myTable = new Table();
      myTable.setBorder(0);
      myTable.setWidth(1,1,"2");
      myTable.mergeCells(2,1,6,1);
      myTable.setWidth("100%");

    PollAnswer[] answers = PollBusiness.getAnswers(pollQuestionID);
    PollQuestion question = null;
    LocalizedText questionLocText = null;
    try {
      question = new PollQuestion(pollQuestionID);
      questionLocText = TextFinder.getLocalizedText(question,_iLocaleID);
    }
    catch (SQLException e) {
      question = null;
    }

    int total = 0;
    int row = 1;
    int current_hits = 0;

    if ( question != null ) {
      Text questionText = new Text(questionLocText.getHeadline());
        questionText.setBold();
      myTable.add(questionText,2,1);

      if (answers != null) {
        this.setOnLoad("window.resizeTo(290,600);");

        if (answers.length > 0) {
          for ( int i = 0; i < answers.length; i++ ) {
            total += answers[i].getHits();
          }
          for (int i = 0 ; i < answers.length ; i++ ) {
            LocalizedText answerLocText = TextFinder.getLocalizedText(answers[i],_iLocaleID);
            if ( answerLocText != null ) {
              ++row;

              float percent = ( (float) answers[i].getHits() / (float) total ) * 100;

              Text answerText = new  Text(answerLocText.getHeadline());
                answerText.setFontSize(1);
              Text hitsText = new Text(Integer.toString(answers[i].getHits()));
                hitsText.setFontSize(1);
              Text percentText = new Text(com.idega.util.text.TextSoap.decimalFormat(percent,1)+"%");
                percentText.setFontSize(1);

              myTable.add(answerText,2,row);
              if ( _showVotes )
                myTable.add(hitsText,4,row);
              myTable.add(percentText,6,row);
            }
          }
        }
      }

      myTable.setWidth(1,row,"2");
      myTable.setWidth(2,row,"500");
      myTable.setWidth(3,row,"2");
      myTable.setWidth(4,row,"2");
      myTable.setWidth(5,row,"2");
      myTable.setWidth(6,row,"2");
      myTable.setWidth(7,row,"2");
      myTable.setAlignment(4,row,"right");
      myTable.setAlignment(6,row,"right");
    }

    return myTable;
  }

  private void showCollection() {

  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

}

