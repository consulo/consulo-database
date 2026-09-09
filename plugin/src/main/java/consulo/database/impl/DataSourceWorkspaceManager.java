package consulo.database.impl;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.component.persist.StoragePathMacros;
import consulo.project.Project;
import consulo.ui.ex.tree.UITreeState;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * @author VISTALL
 * @since 2020-08-19
 */
@Singleton
@State(name = "DataSourceWorkspaceManager", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
@ServiceAPI(ComponentScope.PROJECT)
@ServiceImpl
public class DataSourceWorkspaceManager implements PersistentStateComponent<DataSourceWorkspaceState> {
    public static DataSourceWorkspaceManager getInstance(@Nonnull Project project) {
        return project.getInstance(DataSourceWorkspaceManager.class);
    }

    private final Project myProject;

    private UITreeState myTreeState;

    @Inject
    public DataSourceWorkspaceManager(Project project) {
        myProject = project;
    }

    public void setTreeState(@Nullable UITreeState treeState) {
        myTreeState = treeState;
    }

    @Nullable
    public UITreeState getTreeState() {
        return myTreeState;
    }

    @Nullable
    @Override
    public DataSourceWorkspaceState getState() {
        DataSourceWorkspaceState state = new DataSourceWorkspaceState();
        state.treeState = myTreeState;
        return state;
    }

    @Override
    public void loadState(DataSourceWorkspaceState state) {
        myTreeState = state.treeState;
    }
}
