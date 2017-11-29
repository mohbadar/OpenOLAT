/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium.page.lecture;

import org.olat.selenium.page.graphene.OOGraphene;
import org.olat.user.restapi.UserVO;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * 
 * Initial date: 26 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesToolPage {
	
	private final WebDriver browser;
	
	public LecturesToolPage(WebDriver browser) {
		this.browser = browser;
	}
	
	public LecturesToolPage assertOnParticipantLecturesList() {
		By overviewBy = By.cssSelector("fieldset.o_sel_lecture_participant_overview");
		OOGraphene.waitElement(overviewBy, browser);
		return this;
	}
	
	public LecturesToolPage selectCourseAsParticipant(String course) {
		By selectBy = By.xpath("//table//tr/td/a[contains(@href,'details')][contains(text(),'" + course + "')]");
		OOGraphene.waitElement(selectBy, browser);
		browser.findElement(selectBy).click();
		OOGraphene.waitBusy(browser);
		return this;
	}
	
	public LecturesToolPage assertOnParticipantLectureBlocks() {
		By blocks = By.cssSelector("div.o_sel_lecture_participant_blocks table");
		OOGraphene.waitElement(blocks, browser);
		return this;
	}
	
	public LecturesToolPage assertOnParticipantLectureBlockAbsent(UserVO teacher, String block, String course) {
		By blocks = By.xpath("//div[contains(@class,'o_sel_lecture_participant_blocks')]//table//tr[td[contains(text(),'" + course + "')]][td[contains(text(),'" + block + "')]][td[contains(text(),'" + teacher.getLastName() + "')]]/td/span/i[contains(@class,'o_lectures_rollcall_danger')]");
		OOGraphene.waitElement(blocks, browser);
		return this;
	}

}