package consulo.database.impl.editor.ui;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;

import javax.swing.event.ChangeEvent;
import javax.swing.table.TableColumn;

/**
 * @author VISTALL
 * @since 2019-05-29
 * <p>
 * TableView which support horizontal scrolling from JXTable
 */
public class TableViewWithHScrolling<Item> extends TableView<Item>
{
	/**
	 * field to store the autoResizeMode while interactively setting horizontal
	 * scrollbar to visible.
	 */
	private int oldAutoResizeMode;

	/**
	 * flag to indicate enhanced auto-resize-off behaviour is on. This is
	 * set/reset in setHorizontalScrollEnabled.
	 */
	private boolean intelliMode;

	/**
	 * internal flag indicating that we are in super.doLayout(). (used in
	 * columnMarginChanged to not update the resizingCol's prefWidth).
	 */
	private boolean inLayout;

	public TableViewWithHScrolling(ListTableModel<Item> model)
	{
		super(model);
	}

	// ----------------------- scrollable control

	/**
	 * Sets the enablement of enhanced horizontal scrolling. If enabled, it
	 * toggles an auto-resize mode which always fills the <code>JViewport</code>
	 * horizontally and shows the horizontal scrollbar if necessary.
	 * <p>
	 * <p>
	 * The default value is <code>false</code>.
	 * <p>
	 * <p>
	 * Note: this is <strong>not</strong> a bound property, though it follows
	 * bean naming conventions.
	 * <p>
	 * PENDING: Probably should be... If so, could be taken by a listening
	 * Action as in the app-framework.
	 * <p>
	 * PENDING JW: the name is mis-leading?
	 *
	 * @param enabled a boolean indicating whether enhanced auto-resize mode is
	 *                enabled.
	 * @see #isHorizontalScrollEnabled()
	 */
	public void setHorizontalScrollEnabled(boolean enabled)
	{
		/*
		 * PENDING JW: add a "real" mode? Problematic because there are several
		 * places in core which check for #AUTO_RESIZE_OFF, can't use different
		 * value without unwanted side-effects. The current solution with
		 * tagging the #AUTO_RESIZE_OFF by a boolean flag #intelliMode is
		 * brittle - need to be very careful to turn off again ... Another
		 * problem is to keep the horizontalScrollEnabled toggling action in
		 * synch with this property. Yet another problem is the change
		 * notification: currently this is _not_ a bound property.
		 */
		if(enabled == (isHorizontalScrollEnabled()))
		{
			return;
		}
		boolean old = isHorizontalScrollEnabled();
		if(enabled)
		{
			// remember the resizeOn mode if any
			if(getAutoResizeMode() != AUTO_RESIZE_OFF)
			{
				oldAutoResizeMode = getAutoResizeMode();
			}
			setAutoResizeMode(AUTO_RESIZE_OFF);
			// setAutoResizeModel always disables the intelliMode
			// must set after calling and update the action again
			intelliMode = true;
		}
		else
		{
			setAutoResizeMode(oldAutoResizeMode);
		}
		firePropertyChange("horizontalScrollEnabled", old,
				isHorizontalScrollEnabled());
	}

	/**
	 * Returns the current setting for horizontal scrolling.
	 *
	 * @return the enablement of enhanced horizontal scrolling.
	 * @see #setHorizontalScrollEnabled(boolean)
	 */
	public boolean isHorizontalScrollEnabled()
	{
		return intelliMode && getAutoResizeMode() == AUTO_RESIZE_OFF;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <p>
	 * Overridden for internal bookkeeping related to the enhanced auto-resize
	 * behaviour.
	 * <p>
	 * <p>
	 * Note: to enable/disable the enhanced auto-resize mode use exclusively
	 * <code>setHorizontalScrollEnabled</code>, this method can't cope with it.
	 *
	 * @see #setHorizontalScrollEnabled(boolean)
	 */
	@Override
	public void setAutoResizeMode(int mode)
	{
		if(mode != AUTO_RESIZE_OFF)
		{
			oldAutoResizeMode = mode;
		}
		intelliMode = false;
		super.setAutoResizeMode(mode);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <p>
	 * Overridden to support enhanced auto-resize behaviour enabled and
	 * necessary.
	 *
	 * @see #setHorizontalScrollEnabled(boolean)
	 */
	@Override
	public boolean getScrollableTracksViewportWidth()
	{
		boolean shouldTrack = super.getScrollableTracksViewportWidth();
		if(isHorizontalScrollEnabled())
		{
			return hasExcessWidth();
		}
		return shouldTrack;
	}

	/**
	 * Layouts column width. The exact behaviour depends on the
	 * <code>autoResizeMode</code> property.
	 * <p>
	 * Overridden to support enhanced auto-resize behaviour enabled and
	 * necessary.
	 *
	 * @see #setAutoResizeMode(int)
	 * @see #setHorizontalScrollEnabled(boolean)
	 */
	@Override
	public void doLayout()
	{
		int resizeMode = getAutoResizeMode();
		// fool super...
		if(isHorizontalScrollEnabled() && hasRealizedParent()
				&& hasExcessWidth())
		{
			autoResizeMode = oldAutoResizeMode;
		}
		inLayout = true;
		super.doLayout();
		inLayout = false;
		autoResizeMode = resizeMode;
	}

	/**
	 * @return boolean to indicate whether the table has a realized parent.
	 */
	private boolean hasRealizedParent()
	{
		return (getWidth() > 0) && (getParent() != null)
				&& (getParent().getWidth() > 0);
	}

	/**
	 * PRE: hasRealizedParent()
	 *
	 * @return boolean to indicate whether the table has widths excessing
	 * parent's width
	 */
	private boolean hasExcessWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <p>
	 * Overridden to support enhanced auto-resize behaviour enabled and
	 * necessary.
	 *
	 * @see #setHorizontalScrollEnabled(boolean)
	 */
	@Override
	public void columnMarginChanged(ChangeEvent e)
	{
		if(isEditing())
		{
			removeEditor();
		}
		TableColumn resizingColumn = getResizingColumn();
		// Need to do this here, before the parent's
		// layout manager calls getPreferredSize().
		if(resizingColumn != null && autoResizeMode == AUTO_RESIZE_OFF
				&& !inLayout)
		{
			resizingColumn.setPreferredWidth(resizingColumn.getWidth());
		}
		resizeAndRepaint();
	}

	/**
	 * Returns the column which is interactively resized. The return value is
	 * null if the header is null or has no resizing column.
	 *
	 * @return the resizing column.
	 */
	private TableColumn getResizingColumn()
	{
		return (tableHeader == null) ? null : tableHeader.getResizingColumn();
	}

	/**
	 * {@inheritDoc} <p>
	 * <p>
	 * Overridden for documentation reasons only: same behaviour but different default value.
	 * <p>
	 * <p>
	 * The default value is <code>true</code>.
	 * <p>
	 */
	@Override
	public void setFillsViewportHeight(boolean fillsViewportHeight)
	{
		if(fillsViewportHeight == getFillsViewportHeight())
		{
			return;
		}
		super.setFillsViewportHeight(fillsViewportHeight);
	}
}
