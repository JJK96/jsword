
package org.crosswire.common.swing;

import java.awt.Component;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Hashtable;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;

import org.crosswire.common.util.StringUtil;

/**
* Various Menu creation utilities.
*
* <table border='1' cellPadding='3' cellSpacing='0' width="100%">
* <tr><td bgColor='white'class='TableRowColor'><font size='-7'>
* Distribution Licence:<br />
* Project B is free software; you can redistribute it
* and/or modify it under the terms of the GNU General Public License,
* version 2 as published by the Free Software Foundation.<br />
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* General Public License for more details.<br />
* The License is available on the internet
* <a href='http://www.gnu.org/copyleft/gpl.html'>here</a>, by writing to
* <i>Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
* MA 02111-1307, USA</i>, Or locally at the Licence link below.<br />
* The copyright to this program is held by it's authors.
* </font></td></tr></table>
* @see <a href='http://www.eireneh.com/servlets/Web'>Project B Home</a>
* @see <{docs.Licence}>
* @author Joe Walker
*/
public class MenuUtil
{
    /**
    * Create the menubar for the app.  By default this pulls the
    * definition of the menu from the associated resource file.
    */
    public static void setResourceBundle(ResourceBundle resource)
    {
        MenuUtil.resource = resource;
    }

    /**
    *
    */
    public static void addActions(Action[] actions)
    {
        for (int i=0; i<actions.length; i++)
        {
            commands.put(actions[i].getValue(Action.NAME), actions[i]);
        }
    }

    /**
    * Create the menubar for the app.  By default this pulls the
    * definition of the menu from the associated resource file.
    */
    public static JMenuBar createMenubar()
    {
        JMenuBar menubar = new JMenuBar();

        String[] menu_names = StringUtil.tokenize(getResourceString("menubar"));
        for (int i=0; i<menu_names.length; i++)
        {
            JMenu menu = createMenu(menu_names[i]);
            if (menu != null) menubar.add(menu);
        }
        return menubar;
    }

    /**
    * Create a menu for the app.  By default this pulls the
    * definition of the menu from the associated resource file.
    */
    public static JMenu createMenu(String name)
    {
        String[] item_names = StringUtil.tokenize(getResourceString(name));
        JMenu menu = new JMenu(getResourceString(name+labelSuffix));
        for (int i=0; i<item_names.length; i++)
        {
            if (item_names[i].equals("-"))
            {
                menu.addSeparator();
            }
            else
            {
                JMenuItem menuitem = createMenuItem(item_names[i]);
                menu.add(menuitem);
            }
        }
        return menu;
    }

    /**
    * This is the hook through which all menu items are
    * created.  It registers the result with the menuitem
    * hashtable so that it can be fetched with getMenuItem().
    */
    protected static JMenuItem createMenuItem(String name)
    {
        JMenuItem menuitem = new JMenuItem(getResourceString(name+labelSuffix));
        URL url = getResource(name+imageSuffix);
        if (url != null)
        {
            menuitem.setHorizontalTextPosition(JButton.RIGHT);
            menuitem.setIcon(new ImageIcon(url));
        }
        String action_name = getResourceString(name+actionSuffix);
        if (action_name == null) action_name = name;
        menuitem.setActionCommand(action_name);
        Action action = getAction(action_name);
        if (action != null)
        {
            menuitem.addActionListener(action);
            action.addPropertyChangeListener(createActionChangeListener(menuitem));
            menuitem.setEnabled(action.isEnabled());
        }
        else
        {
            menuitem.setEnabled(false);
        }
        menuitems.put(name, menuitem);
        return menuitem;
    }
    protected static Action getAction(String cmd)
    {
        return (Action) commands.get(cmd);
    }

    /**
    * Fetch the menu item that was created for the given
    * command.
    * @param cmd  Name of the action.
    * @returns item created for the given command or null
    *  if one wasn't created.
    */
    protected JMenuItem getMenuItem(String cmd)
    {
        return (JMenuItem) menuitems.get(cmd);
    }

    /**
    * Create the toolbar.  By default this reads the
    * resource file for the definition of the toolbar.
    */
    public static Component createToolbar()
    {
        JToolBar toolbar = new JToolBar();
        String[] toolKeys = StringUtil.tokenize(getResourceString("toolbar"));
        for (int i = 0; i < toolKeys.length; i++)
        {
            if (toolKeys[i].equals("-"))
            {
                toolbar.add(Box.createHorizontalStrut(5));
            }
            else
            {
                toolbar.add(createTool(toolKeys[i]));
            }
        }
        toolbar.add(Box.createHorizontalGlue());
        return toolbar;
    }

    /**
     * Hook through which every toolbar item is created.
     */
    protected static Component createTool(String key)
    {
        return createToolbarButton(key);
    }

    /**
     * Create a button to go inside of the toolbar.  By default this
     * will load an image resource.  The image filename is relative to
     * the classpath (including the '.' directory if its a part of the
     * classpath), and may either be in a JAR file or a separate file.
     *
     * @param key The key in the resource file to serve as the basis
     *  of lookups.
     */
    protected static JButton createToolbarButton(String key)
    {
        URL url = getResource(key + imageSuffix);
        JButton button = new JButton(new ImageIcon(url)
        {
            public float getAlignmentY() { return 0.5f; }
        });
        button.setRequestFocusEnabled(false);
        button.setMargin(new Insets(1,1,1,1));

        String astr = getResourceString(key + actionSuffix);
        if (astr == null) astr = key;
        Action a = getAction(astr);
        if (a != null)
        {
            button.setActionCommand(astr);
            button.addActionListener(a);
        }
        else
        {
            button.setEnabled(false);
        }

        String tip = getResourceString(key + tipSuffix);
        if (tip != null) button.setToolTipText(tip);

        return button;
    }

    // Yarked from JMenu, ideally this would be public.
    protected static PropertyChangeListener createActionChangeListener(JMenuItem b)
    {
        return new ActionChangedListener(b);
    }

    // Yarked from JMenu, ideally this would be public.
    private static class ActionChangedListener implements PropertyChangeListener
    {
        JMenuItem menuItem;

        ActionChangedListener(JMenuItem mi)
        {
            super();
            this.menuItem = mi;
        }

        public void propertyChange(PropertyChangeEvent ev)
        {
            String propertyName = ev.getPropertyName();

            if (ev.getPropertyName().equals(Action.NAME))
            {
                String text = (String) ev.getNewValue();
                menuItem.setText(text);
            }
            else if (propertyName.equals("enabled"))
            {
                Boolean enabled = (Boolean) ev.getNewValue();
                menuItem.setEnabled(enabled.booleanValue());
            }
        }
    }

    /**
    * Get a string from a resource bundle
    */
    protected static String getResourceString(String name)
    {
        try
        {
            return resource.getString(name);
        }
        catch (MissingResourceException ex)
        {
            return null;
        }
    }
    /**
    * Get a URL from a resource bundle
    */
    protected static URL getResource(String key)
    {
        String name = getResourceString(key);
        if (name == null) return null;

        return resource.getClass().getResource(name);
    }
    /**
    * Suffix applied to the key used in resource file
    * lookups for an image.
    */
    public static final String imageSuffix = "Image";

    /**
    * Suffix applied to the key used in resource file
    * lookups for a label.
    */
    public static final String labelSuffix = "Label";

    /**
    * Suffix applied to the key used in resource file
    * lookups for an action.
    */
    public static final String actionSuffix = "Action";

    /**
    * Suffix applied to the key used in resource file
    * lookups for tooltip text.
    */
    public static final String tipSuffix = "Tooltip";

    /*
    * Data
    */
    private static Hashtable menuitems = new Hashtable();
    private static Hashtable commands = new Hashtable();
    private static ResourceBundle resource = null;
}
