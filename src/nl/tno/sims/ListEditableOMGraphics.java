package nl.tno.sims;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import com.bbn.openmap.omGraphics.EditableOMGraphic;
import com.bbn.openmap.omGraphics.GrabPoint;
import com.bbn.openmap.proj.Projection;

import org.apache.commons.lang.ArrayUtils;
/**
 * 
 * This list will return all grabpoints of the subelements, so single objects can be moved instead of the entire list
 * @author kromppjd
 *
 */
public class ListEditableOMGraphics  {
	
	private Hashtable<String, EditableOMGraphic> editables = new Hashtable<String, EditableOMGraphic>();
	
	private EditableOMGraphic mSelectedObject = null;
	private int mSelectedPointIndex = -1;
	
	public ListEditableOMGraphics() {
	}
	
	public void clear() {
		editables.clear();
	}
	

    /**
     * Set the current projection.
     */
    public void setProjection(Projection proj) {
    	synchronized (editables) {
			for (Iterator<EditableOMGraphic> it = editables.values().iterator(); it.hasNext();) {
	        	EditableOMGraphic g = it.next();
	        	g.setProjection(proj);
	        	g.generate(proj);
	        }
    	}
    }

	public void add(String name, EditableOMGraphic g) {
		editables.put(name, g);
	}

	public void render(Graphics g) {
    	synchronized (editables) {
	        for (Iterator<EditableOMGraphic> it = editables.values().iterator(); it.hasNext();) {
	        	it.next().render(g);
	        }
    	}
	}

	public GrabPoint getGrabPoint(MouseEvent e) {
    	GrabPoint gp = null;
    	mSelectedObject=null;
    	mSelectedPointIndex=-1;
    	synchronized (editables) {
	        for (Iterator<EditableOMGraphic> it = editables.values().iterator(); it.hasNext();) {
	        	EditableOMGraphic g = it.next();
	        	gp = g._getMovingPoint(e);
	        	if (gp!=null) {
	        		mSelectedObject = g;
	        		mSelectedPointIndex = ArrayUtils.indexOf(g.getGrabPoints(),gp);
	        		return gp;
	        	}
	        }
    	}
		return gp;
	}

	public EditableOMGraphic getSelectedGraphic() {
		return mSelectedObject;
	}
	
	public EditableOMGraphic getGraphic(String sName) {
		return editables.get(sName);
	}
		
	public void removeGraphic(String sName) {
		editables.remove(sName);
	}

public int getSelectedPointIndex() {
		return mSelectedPointIndex;
	}
	
}
