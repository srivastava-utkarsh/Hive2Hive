package org.hive2hive.core.process;

import java.util.ArrayList;
import java.util.List;

import org.hive2hive.core.process.listener.IProcessListener;

/**
 * Helps building process trees. A child process starts when the parent process has successfully finished
 * 
 * @author Nico
 * 
 */
public abstract class ProcessTreeNode extends Process {

	private final Process process;
	private final List<ProcessTreeNode> childProcesses;
	private boolean done;
	private final ProcessTreeNode parent;

	/**
	 * For the root node (does not do anything except holding children and starting them simultanously
	 */
	public ProcessTreeNode() {
		this(null, null);
	}

	/**
	 * For process nodes that hold process. The process is not started unil the parent has finished
	 * 
	 * @param process
	 * @param parent
	 * @param node
	 */
	public ProcessTreeNode(Process process, ProcessTreeNode parent) {
		super(null);
		this.process = process;
		this.parent = parent;
		this.childProcesses = new ArrayList<ProcessTreeNode>();
		this.done = false;
		if (parent != null) {
			parent.addChild(this);
		}
	}

	public void addChild(ProcessTreeNode childProcess) {
		childProcesses.add(childProcess);
	}

	public List<ProcessTreeNode> getChildren() {
		return childProcesses;
	}

	public ProcessTreeNode getParent() {
		return parent;
	}

	/**
	 * Returns whether all children are done as well
	 * 
	 * @return
	 */
	public boolean isDone() {
		boolean allChildrenDone = true;
		for (ProcessTreeNode child : childProcesses) {
			allChildrenDone &= child.isDone();
		}

		return done && allChildrenDone;
	}

	public int getDepth() {
		if (parent == null) {
			return 0;
		} else {
			return parent.getDepth() + 1;
		}
	}

	@Override
	public void run() {
		if (process == null) {
			setNextStep(null);

			// is root node --> start all children
			for (ProcessTreeNode child : childProcesses) {
				child.start();
			}
			// no further tasks for the root node
			done = true;
		} else {
			// after the current process is done, start the next process
			process.addListener(new IProcessListener() {
				@Override
				public void onSuccess() {
					done = true;
					for (ProcessTreeNode child : childProcesses) {
						child.start();
					}
				}

				@Override
				public void onFail(String reason) {
					done = true;
					onFail(reason);
				}
			});

			process.start();
		}
	}

	public abstract void onFail(String reason);
}
