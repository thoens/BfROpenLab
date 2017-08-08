/*******************************************************************************
 * Copyright (c) 2017 German Federal Institute for Risk Assessment (BfR)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Department Biological Safety - BfR
 *******************************************************************************/
/**
 * This class is generated by jOOQ
 */
package de.bund.bfr.knime.openkrise.db.generated.public_.tables;


import de.bund.bfr.knime.openkrise.db.generated.public_.Keys;
import de.bund.bfr.knime.openkrise.db.generated.public_.Public;
import de.bund.bfr.knime.openkrise.db.generated.public_.tables.records.ExtrafieldsRecord;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
	value = {
		"http://www.jooq.org",
		"jOOQ version:3.6.2"
	},
	comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Extrafields extends TableImpl<ExtrafieldsRecord> {

	private static final long serialVersionUID = -373302160;

	/**
	 * The reference instance of <code>PUBLIC.ExtraFields</code>
	 */
	public static final Extrafields EXTRAFIELDS = new Extrafields();

	/**
	 * The class holding records for this type
	 */
	@Override
	public Class<ExtrafieldsRecord> getRecordType() {
		return ExtrafieldsRecord.class;
	}

	/**
	 * The column <code>PUBLIC.ExtraFields.tablename</code>.
	 */
	public final TableField<ExtrafieldsRecord, String> TABLENAME = createField("tablename", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.ExtraFields.id</code>.
	 */
	public final TableField<ExtrafieldsRecord, Integer> ID = createField("id", org.jooq.impl.SQLDataType.INTEGER, this, "");

	/**
	 * The column <code>PUBLIC.ExtraFields.attribute</code>.
	 */
	public final TableField<ExtrafieldsRecord, String> ATTRIBUTE = createField("attribute", org.jooq.impl.SQLDataType.VARCHAR.length(255), this, "");

	/**
	 * The column <code>PUBLIC.ExtraFields.value</code>.
	 */
	public final TableField<ExtrafieldsRecord, String> VALUE = createField("value", org.jooq.impl.SQLDataType.VARCHAR.length(32768), this, "");

	/**
	 * Create a <code>PUBLIC.ExtraFields</code> table reference
	 */
	public Extrafields() {
		this("ExtraFields", null);
	}

	/**
	 * Create an aliased <code>PUBLIC.ExtraFields</code> table reference
	 */
	public Extrafields(String alias) {
		this(alias, EXTRAFIELDS);
	}

	private Extrafields(String alias, Table<ExtrafieldsRecord> aliased) {
		this(alias, aliased, null);
	}

	private Extrafields(String alias, Table<ExtrafieldsRecord> aliased, Field<?>[] parameters) {
		super(alias, Public.PUBLIC, aliased, parameters, "");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<UniqueKey<ExtrafieldsRecord>> getKeys() {
		return Arrays.<UniqueKey<ExtrafieldsRecord>>asList(Keys.EXTRAFIELDS_UNI_0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Extrafields as(String alias) {
		return new Extrafields(alias, this);
	}

	/**
	 * Rename this table
	 */
	public Extrafields rename(String name) {
		return new Extrafields(name, null);
	}
}
