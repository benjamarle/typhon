package net.zorgblub.ui;

import java.util.ArrayList;
import java.util.List;

import jedi.option.Option;

import static java.util.Collections.unmodifiableList;
import static jedi.functional.FunctionalPrimitives.forEach;
import static net.zorgblub.typhon.CollectionUtil.listElement;

/**
 * Created by alex on 11/9/14.
 */
public class TreeNode<T extends TreeNode> {

    private List<T> children = new ArrayList<>();

    public TreeNode() {}

    public TreeNode( List<T> children ) {
        this.children.addAll( children );
    }

    public void addChild( T child ) {
        this.children.add( child );
    }

    public boolean hasChildren() {
        return ! this.children.isEmpty();
    }

    public Option<T> getChild( int child ) {
        return listElement( children, child );
    }

    public int getChildCount() {
        return children.size();
    }

    public void addChildren( Iterable<T> children ) {
        forEach( children, this::addChild );
    }

    public List<T> getChildren() {
        return unmodifiableList( this.children );
    }

}
