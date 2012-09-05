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
package org.olat.course.member;

import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CourseRoleCellRenderer implements CustomCellRenderer {
	
	private final Translator translator;
	
	public CourseRoleCellRenderer(Locale locale) {
		translator = Util.createPackageTranslator(CourseRoleCellRenderer.class, locale);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if (val instanceof CourseMembership) {
			CourseMembership membership = (CourseMembership)val;
			
			boolean and = false;
			if(membership.isOwner()) {
				and = and(sb, and);
				sb.append(translator.translate("role.owner"));
			}
			if(membership.isTutor()) {
				and = and(sb, and);
				sb.append(translator.translate("role.tutor"));
			}
			if(membership.isParticipant()) {
				and = and(sb, and);
				sb.append(translator.translate("role.participant"));
			}
			if(membership.isWaiting()) {
				and = and(sb, and);
				sb.append(translator.translate("role.waiting"));
			}
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
