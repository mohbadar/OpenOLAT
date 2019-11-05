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
package org.olat.course.learningpath.evaluation;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 26 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DefaultLinearStatusEvaluator implements StatusEvaluator {

	private static final Logger log = Tracing.createLoggerFor(DefaultLinearStatusEvaluator.class);

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			Blocker blocker) {
		AssessmentEntryStatus currentStatus = currentEvaluation.getAssessmentStatus();
		AssessmentEntryStatus status = currentStatus;
		if (isNotInProgressYet(currentStatus)) {
			if (isNotBlocked(blocker)) {
				status = AssessmentEntryStatus.notStarted;
			} else {
				status = AssessmentEntryStatus.notReady;
			}
		}
		if (blocker != null && AssessmentObligation.mandatory.equals(currentEvaluation.getObligation())
				&& !Boolean.TRUE.equals(currentEvaluation.getFullyAssessed())) {
			blocker.block();
		}

		log.debug("fully assessed '{}', obligation '{}, blocked: '{}', status '{}', new status '{}'"
				, currentEvaluation.getFullyAssessed()
				, currentEvaluation.getObligation()
				, !isNotBlocked(blocker)
				, currentEvaluation.getAssessmentStatus()
				, status);

		return status;
	}

	private boolean isNotInProgressYet(AssessmentEntryStatus currentStatus) {
		return currentStatus == null
				|| AssessmentEntryStatus.notReady.equals(currentStatus)
				|| AssessmentEntryStatus.notStarted.equals(currentStatus);
	}

	private boolean isNotBlocked(Blocker blocker) {
		return blocker == null || !blocker.isBlocked();
	}

	@Override
	public AssessmentEntryStatus getStatus(AssessmentEvaluation currentEvaluation,
			List<AssessmentEvaluation> children) {
		return currentEvaluation.getAssessmentStatus();
	}

}
