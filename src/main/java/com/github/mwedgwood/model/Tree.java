package com.github.mwedgwood.model;

import com.google.common.collect.Range;
import com.sun.istack.internal.NotNull;
import org.hibernate.Hibernate;
import org.hibernate.envers.AuditMappedBy;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Entity
@Audited
public class Tree {

    private Integer id;
    private String name;
    private Integer childOrder = 0;

    private Tree parent;
    private List<Tree> children = new ArrayList<>();

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public Tree setId(Integer id) {
        this.id = id;
        return this;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public Tree setName(String name) {
        this.name = name;
        return this;
    }

    @ManyToOne
    public Tree getParent() {
        return parent;
    }

    private Tree setParent(Tree parent) {
        this.parent = parent;
        return this;
    }

    @Column(name = "child_order")
    public Integer getChildOrder() {
        return childOrder;
    }

    private Tree setChildOrder(Integer childOrder) {
        this.childOrder = childOrder;
        return this;
    }

    @AuditMappedBy(mappedBy = "parent", positionMappedBy = "childOrder")
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    public List<Tree> getChildren() {
        return children;
    }

    public Tree setChildren(List<Tree> children) {
        this.children = children;
        return this;
    }

    public Tree addChildTree(Tree childTree) {
        children.add(childTree);
        childTree.setChildOrder(children.size() - 1);
        return childTree.setParent(this);
    }

    public Tree addChildTree(Tree childTree, Integer order) {
        int index = (order != null && Range.closedOpen(0, children.size()).contains(order)) ? order : children.size();
        children.add(index, childTree);
        for (int newIndex = 0; newIndex < children.size(); newIndex++) {
            children.get(newIndex).setChildOrder(newIndex);
        }
        return childTree.setParent(this);
    }

    public String prettyPrint() {
        return prettyPrint(this, "", true).trim();
    }

    private String prettyPrint(Tree tree, String prefix, boolean isTail) {
        StringBuilder stringBuilder = new StringBuilder(prefix).append((isTail ? "└── " : "├── ")).append(tree.name).append("\n");
        if (!Hibernate.isInitialized(tree.children)) return stringBuilder.toString();

        for (Iterator<Tree> iterator = tree.children.iterator(); iterator.hasNext(); ) {
            stringBuilder.append(prettyPrint(iterator.next(), prefix + (isTail ? "    " : "│   "), !iterator.hasNext()));
        }
        return stringBuilder.toString();
    }
}
