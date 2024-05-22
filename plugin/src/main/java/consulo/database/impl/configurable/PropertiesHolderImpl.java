/*
 * Copyright 2013-2020 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.database.impl.configurable;

import consulo.credentialStorage.PasswordSafe;
import consulo.credentialStorage.PasswordSafeException;
import consulo.database.datasource.configurable.GenericPropertyKey;
import consulo.database.datasource.configurable.PropertiesHolder;
import consulo.database.datasource.configurable.SecureString;
import consulo.util.lang.ObjectUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import org.jdom.Element;

import java.util.HashMap;
import java.util.Map;

/**
 * @author VISTALL
 * @since 2020-08-16
 */
public class PropertiesHolderImpl implements PropertiesHolder
{
	public static final String TAG_NAME = "property-container";

	protected static class UnstableValue
	{
		public Object value = ObjectUtil.NULL;

		public String xmlValue;

		public UnstableValue(String key, Object value)
		{
			this.value = value;

			if(value instanceof SecureString.RawSecureString rawSecureString)
			{
				try
				{
					PasswordSafe.getInstance().storePassword(null, StoreSecureStringImpl.class, key, rawSecureString.getRawValue(), true);
				}
				catch(PasswordSafeException ignored)
				{
					// log?
				}
			}
		}

		@SuppressWarnings("unchecked")
		public <T> T get(GenericPropertyKey<T> key)
		{
			if(value != ObjectUtil.NULL)
			{
				return (T) value;
			}

			if(xmlValue != null)
			{
				if(key.getTypeClass() == Integer.class)
				{
					T parsed = (T) Integer.valueOf(Integer.parseInt(xmlValue));
					value = parsed;
					xmlValue = null;
					return parsed;
				}

				if(key.getTypeClass() == String.class)
				{
					value = xmlValue;
					xmlValue = null;
					return (T) value;
				}

				if(key.getTypeClass() == SecureString.class)
				{
					value = new StoreSecureStringImpl(xmlValue);
					xmlValue = null;
					return (T) value;
				}

				throw new UnsupportedOperationException("Key type " + key.getTypeClass() + " is not supported");
			}

			return key.getDefautValue();
		}

		public String getRawStringValue(String key)
		{
			if(value instanceof SecureString.RawSecureString rawStringValue)
			{
				return rawStringValue.getStoreValue(key);
			}

			if(value != ObjectUtil.NULL)
			{
				return String.valueOf(value);
			}

			return xmlValue;
		}
	}

	protected Map<String, UnstableValue> myValues = new HashMap<>();

	private final String myName;

	public PropertiesHolderImpl(@Nonnull String name)
	{
		myName = name;
	}

	@Override
	@SuppressWarnings("unchecked")
	@Nullable
	public <T> T get(@Nonnull GenericPropertyKey<T> key)
	{
		UnstableValue value = myValues.get(key.toString());
		if(value == null)
		{
			return key.getDefautValue();
		}
		T getValue = value.get(key);
		if(getValue == null)
		{
			return key.getDefautValue();
		}
		return getValue;
	}

	public void copyFrom(PropertiesHolderImpl other)
	{
		myValues.clear();
		myValues.putAll(other.myValues);
	}

	@Nonnull
	public Element toXmlState()
	{
		Element root = new Element(TAG_NAME);
		root.setAttribute("name", myName);

		for(Map.Entry<String, UnstableValue> entry : myValues.entrySet())
		{
			Element propertyElement = new Element("property");
			String key = entry.getKey();
			propertyElement.setAttribute("name", key);
			propertyElement.setAttribute("value", String.valueOf(entry.getValue().getRawStringValue(key)));

			root.addContent(propertyElement);
		}

		return root;
	}

	public void fromXmlState(@Nonnull Element element)
	{
		for(Element propertyElement : element.getChildren("property"))
		{
			String name = propertyElement.getAttributeValue("name");

			String value = propertyElement.getAttributeValue("value");

			UnstableValue unstableValue = new UnstableValue(name, ObjectUtil.NULL);
			unstableValue.xmlValue = value;
			myValues.put(name, unstableValue);
		}
	}
}
