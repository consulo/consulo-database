package consulo.database.impl.editor;

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.UnnamedConfigurable;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.NamedConfigurable;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.components.BorderLayoutPanel;
import consulo.database.datasource.model.EditableDataSource;
import consulo.database.datasource.transport.DataSourceTransportManager;
import consulo.options.ConfigurableUIMigrationUtil;
import consulo.ui.annotation.RequiredUIAccess;
import org.jetbrains.annotations.Nls;

import javax.swing.*;
import java.awt.*;

/**
 * @author VISTALL
 * @since 2020-08-13
 */
public class DataSourceConfigurable extends NamedConfigurable<EditableDataSource>
{
	private final Project myProject;

	private final EditableDataSource myDataSource;

	private UnnamedConfigurable myInnerConfigurable;

	public DataSourceConfigurable(Project project, EditableDataSource dataSource, Runnable treeUpdater)
	{
		super(true, treeUpdater);
		myProject = project;
		myDataSource = dataSource;
	}

	@RequiredUIAccess
	@Override
	public boolean isModified()
	{
		return myInnerConfigurable != null && myInnerConfigurable.isModified();
	}

	@RequiredUIAccess
	@Override
	public void apply() throws ConfigurationException
	{
		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.apply();
		}
	}

	@RequiredUIAccess
	@Override
	public void reset()
	{
		// need create ui
		createComponent();

		if(myInnerConfigurable != null)
		{
			myInnerConfigurable.reset();
		}
	}

	@Nls
	@Override
	public String getDisplayName()
	{
		return myDataSource.getName();
	}

	@Override
	public void setDisplayName(String name)
	{
		myDataSource.setName(name);
	}

	@Override
	public EditableDataSource getEditableObject()
	{
		return null;
	}

	@Override
	public String getBannerSlogan()
	{
		return null;
	}

	@Override
	@RequiredUIAccess
	public JComponent createOptionsPanel()
	{
		if(myInnerConfigurable == null)
		{
			myInnerConfigurable = myDataSource.getProvider().createConfigurable(myDataSource);
		}

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(ConfigurableUIMigrationUtil.createComponent(myInnerConfigurable), BorderLayout.CENTER);

		JButton testButton = new JButton("Test Connection");
		testButton.addActionListener(e ->
		{
			try
			{
				apply();
			}
			catch(ConfigurationException ignored)
			{
				return;
			}

			DataSourceTransportManager dataSourceTransportManager = DataSourceTransportManager.getInstance(myProject);

			AsyncResult<Void> result = dataSourceTransportManager.testConnection(myDataSource);

			result.doWhenDone(() -> {
				SwingUtilities.invokeLater(() -> Messages.showInfoMessage(myProject, "Connection success", "DataSource"));
			});

			result.doWhenRejectedWithThrowable(throwable -> {
				if(throwable instanceof ProcessCanceledException)
				{
					// canceled no need info
					return;
				}
				SwingUtilities.invokeLater(() -> Messages.showErrorDialog(myProject, "Connection failed: " + throwable.getMessage(), "DataSource"));
			});
		});

		JPanel buttonPanel = new BorderLayoutPanel().addToRight(testButton);
		buttonPanel.setBorder(JBUI.Borders.empty(0, 10));
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}
}
