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

package consulo.database.datasource.jdbc.transport;

import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import consulo.util.dataholder.UnprotectedUserDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;

/**
 * @author VISTALL
 * @since 2020-08-16
 *
 * TODO not good idea
 */
public class FakeSdk extends UnprotectedUserDataHolder implements Sdk
{
	private static SimpleJavaSdkType ourJavaSdkType = new SimpleJavaSdkType();

	private final String myHomePath;

	public FakeSdk(String homePath)
	{
		myHomePath = homePath;
	}

	@Override
	public FakeSdk clone() throws CloneNotSupportedException
	{
		return new FakeSdk(myHomePath);
	}

	@Nonnull
	@Override
	public SdkTypeId getSdkType()
	{
		return ourJavaSdkType;
	}

	@Override
	public boolean isPredefined()
	{
		return false;
	}

	@Nonnull
	@Override
	public String getName()
	{
		return "stub";
	}

	@Nullable
	@Override
	public String getVersionString()
	{
		return ourJavaSdkType.getVersionString(getHomePath());
	}

	@Nullable
	@Override
	public String getHomePath()
	{
		return myHomePath;
	}

	@Nullable
	@Override
	public VirtualFile getHomeDirectory()
	{
		return LocalFileSystem.getInstance().findFileByIoFile(new File(myHomePath));
	}

	@Nonnull
	@Override
	public RootProvider getRootProvider()
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public SdkModificator getSdkModificator()
	{
		throw new UnsupportedOperationException();
	}

	@Nullable
	@Override
	public SdkAdditionalData getSdkAdditionalData()
	{
		throw new UnsupportedOperationException();
	}
}
