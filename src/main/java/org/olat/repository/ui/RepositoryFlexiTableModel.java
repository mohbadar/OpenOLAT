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
package org.olat.repository.ui;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryFlexiTableModel extends DefaultFlexiTableDataModel<RepositoryEntry>
implements SortableFlexiTableDataModel<RepositoryEntry> {
	
	private final Translator translator;
	private final Map<String,String> fullNames = new HashMap<>();
	private final Map<Long,OLATResourceAccess> repoEntriesWithOffer = new HashMap<>();
	
	@Autowired
	private ACService acService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AccessControlModule acModule;
	
	public RepositoryFlexiTableModel(FlexiTableColumnModel columnModel, Locale locale) {
		super(columnModel);
		CoreSpringFactory.autowireObject(this);
		translator = Util.createPackageTranslator(RepositoryService.class, locale);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		if(orderBy != null) {
			RepositoryFlexiTableSortDelegate sort = new RepositoryFlexiTableSortDelegate(orderBy, this, translator.getLocale());
			List<RepositoryEntry> sorted = sort.sort();
			super.setObjects(sorted);
		}
	}
	
	@Override
	public Object getValueAt(int row, int col) {
		RepositoryEntry entry = getObject(row);
		return getValueAt(entry, col);
	}

	@Override
	public Object getValueAt(RepositoryEntry re, int col) {
		switch (RepoCols.values()[col]) {
			case ac: return getAccessControl(re);
			case repoEntry: return re; 
			case displayname: return getDisplayName(re, translator.getLocale());
			case author: return getFullname(re.getInitialAuthor());
			case access: return re;
			case creationDate: return re.getCreationDate();
			case lastUsage: return re.getStatistics().getLastUsage();
			case externalId: return re.getExternalId();
			case externalRef: return re.getExternalRef();
			case lifecycleLabel: {
				RepositoryEntryLifecycle lf = re.getLifecycle();
				if(lf == null || lf.isPrivateCycle()) {
					return "";
				}
				return lf.getLabel();
			}
			case lifecycleSoftKey: {
				RepositoryEntryLifecycle lf = re.getLifecycle();
				if(lf == null || lf.isPrivateCycle()) {
					return "";
				}
				return lf.getSoftKey();
			}
			case lifecycleStart: return re.getLifecycle() == null ? null : re.getLifecycle().getValidFrom();
			case lifecycleEnd: return re.getLifecycle() == null ? null : re.getLifecycle().getValidTo();
			default: return "ERROR";
		}
	}
	
	@Override
	public void setObjects(List<RepositoryEntry> objects) {
		super.setObjects(objects);
		repoEntriesWithOffer.clear();
		secondaryInformations(objects);
	}
	
	public void addObject(RepositoryEntry object) {
		getObjects().add(object);
		secondaryInformations(Collections.singletonList(object));
	}
	
	public void addObjects(List<RepositoryEntry> addedObjects) {
		getObjects().addAll(addedObjects);
		secondaryInformations(addedObjects);
	}
	
	private void secondaryInformations(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty()) return;
		
		secondaryInformationsAccessControl(repoEntries);
		secondaryInformationsUsernames(repoEntries);
	}

	private void secondaryInformationsAccessControl(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty() || !acModule.isEnabled()) return;

		List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(repoEntries);
		for(OLATResourceAccess withOffer:withOffers) {
			repoEntriesWithOffer.put(withOffer.getResource().getKey(), withOffer);
		}
	}
	
	private void secondaryInformationsUsernames(List<RepositoryEntry> repoEntries) {
		if(repoEntries == null || repoEntries.isEmpty()) return;

		Set<String> newNames = new HashSet<>();
		for(RepositoryEntry re:repoEntries) {
			final String author = re.getInitialAuthor();
			if(StringHelper.containsNonWhitespace(author) &&
				!fullNames.containsKey(author)) {
				newNames.add(author);
			}
		}
		
		if(!newNames.isEmpty()) {
			Map<String,String> newFullnames = userManager.getUserDisplayNamesByUserName(newNames);
			fullNames.putAll(newFullnames);
		}
	}
	
	public void removeObject(RepositoryEntry object) {
		getObjects().remove(object);
		repoEntriesWithOffer.remove(object.getOlatResource().getKey());
	}
	
	private Object getAccessControl(RepositoryEntry re) {
		if (!re.isAllUsers() && !re.isGuests()) {
			// members only always show lock icon
			return Collections.singletonList("o_ac_membersonly");
		}
		OLATResourceAccess access = repoEntriesWithOffer.get(re.getOlatResource().getKey());
		if(access == null) {
			return null;						
		}
		return access;
	}
	
	private String getFullname(String author) {
		if(fullNames.containsKey(author)) {
			return fullNames.get(author);
		}
		return author;
	}

	/**
	 * Get displayname of a repository entry. If repository entry a course 
	 * and is this course closed then add a prefix to the title.
	 */
	private String getDisplayName(RepositoryEntry repositoryEntry, Locale locale) {
		String displayName = repositoryEntry.getDisplayname();
		if (repositoryEntry.getEntryStatus().decommissioned()) {
			Translator pT = Util.createPackageTranslator(RepositoryModule.class, locale);
			displayName = "[" + pT.translate("title.prefix.closed") + "] ".concat(displayName);
		}
		return displayName;
	}

	@Override
	public DefaultFlexiTableDataModel<RepositoryEntry> createCopyWithEmptyList() {
		return new RepositoryFlexiTableModel(getTableColumnModel(), translator.getLocale());
	}

	public enum RepoCols implements FlexiSortableColumnDef {
		ac("table.header.ac"),
		repoEntry("table.header.typeimg"),
		displayname("table.header.displayname"),
		author("table.header.author"),
		access("table.header.access"),
		creationDate("table.header.date"),
		lastUsage("table.header.lastusage"),
		externalId("table.header.externalid"),
		externalRef("table.header.externalref"),
		lifecycleLabel("table.header.lifecycle.label"),
		lifecycleSoftKey("table.header.lifecycle.softkey"),
		lifecycleStart("table.header.lifecycle.start"),
		lifecycleEnd("table.header.lifecycle.end");
		
		private final String i18nKey;
		
		private RepoCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
