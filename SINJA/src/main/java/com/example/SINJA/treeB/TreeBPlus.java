package com.example.SINJA.treeB;

import com.example.SINJA.model.Node;
import com.example.SINJA.model.Tuple;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TreeBPlus {

    Node root;

    public TreeBPlus(){
        this.root = new Node();
    }


    private void insert(Tuple data, Node node){

        Node father = node.getFather();
        //Validamos que estemos en una hoja para insertar
        if (node.getLinks().isEmpty()){
            if (node.getData().size() < (2 * node.getOrder())){
                node.getData().add(data);
                node.getData().sort(Comparator.comparing(Tuple::getKey));

             //Si la hoja esta llena hacemos split
            }else{
                //Si la hoja era la raiz, solo creamos un padre y hacemos los enlaces pertinentes
                if(father == null){
                    ArrayList<Tuple> aux = new ArrayList<>(node.getData());
                    aux.add(data);
                    aux.sort(Comparator.comparing(Tuple::getKey));
                    Node rootAux = new Node();
                    rootAux.getData().add(aux.get(node.getOrder()));
                    Node leftAux = new Node();
                    Node rightAux = new Node();

                    for ( int i = 0; i < node.getOrder(); i++ ){
                        leftAux.getData().add(aux.get(i));
                    }

                    for (int i = node.getOrder() + 1; i < aux.size(); i++){
                        rightAux.getData().add(aux.get(i));
                    }
                    rootAux.getLinks().add(leftAux);
                    rootAux.getLinks().add(rightAux);
                    leftAux.setFather(rootAux);
                    rightAux.setFather(rootAux);
                    sheetLinks(node, leftAux, rightAux);
                    this.root = rootAux;
                }else{
                    //Si la hoja no era una raiz, miramos si el padre tiene espacio y le pasamos la clave
                    ArrayList<Tuple> aux = new ArrayList<>(node.getData());
                    aux.add(data);
                    aux.sort(Comparator.comparing(Tuple::getKey));
                    Tuple split = aux.get(node.getOrder());
                    Node leftAux = new Node();
                    Node rightAux = new Node();

                    for ( int i = 0; i < node.getOrder(); i++ ){
                        leftAux.getData().add(aux.get(i));
                    }

                    for (int i = node.getOrder(); i < aux.size(); i++){
                        rightAux.getData().add(aux.get(i));
                    }

                    if (father.getData().size() < 2 * node.getOrder()){
                        father.getData().add(split);
                        father.getData().sort(Comparator.comparing(Tuple::getKey));

                        int oldSheet = father.getLinks().indexOf(node);
                        father.getLinks().set(oldSheet, leftAux);
                        father.getLinks().add(oldSheet + 1, rightAux);
                        leftAux.setFather(father);
                        rightAux.setFather(father);
                        sheetLinks(node, leftAux, rightAux);
                    }else{
                        //Si el padre no tiene espacio debemos hacer splits de nodos internos
                        sheetLinks(node, leftAux, rightAux);
                        splitInternal(father, split, leftAux, rightAux, node);


                    }

                }
            }
        }else{
            int count = 0;
            for (count = 0; count < node.getData().size(); count++){
                if (data.getKey() < node.getData().get(count).getKey()){
                 break;
                }
            }
            insert(data, node.getLinks().get(count));
        }
    }


    private void splitInternal(Node node, Tuple split, Node nodeLeft, Node nodeRight, Node lastNode) {
        ArrayList<Tuple> tempKeys = new ArrayList<>(node.getData());
        ArrayList<Node> tempChildren = new ArrayList<>(node.getLinks());

        // Insertar la tupla que subio desde los hijos
        tempKeys.add(split);
        tempKeys.sort(Comparator.comparing(Tuple::getKey));

        // Ubicar donde estaba el hijo que fue dividido
        int oldChildIndex = tempChildren.indexOf(lastNode);

        // Reemplazar el hijo viejo por los dos nuevos
        tempChildren.set(oldChildIndex, nodeLeft);
        tempChildren.add(oldChildIndex + 1, nodeRight);

        // Elegir la clave que va subir
        int m = node.getOrder();
        Tuple promoted = tempKeys.get(m);


       //Crear dos nodos para dividir el que supero la cantidad de claves
        Node leftNode = new Node();
        Node rightNode = new Node();

        // Claves  a la izquierda
        for (int i = 0; i < m; i++) {
            leftNode.getData().add(tempKeys.get(i));
        }

        // Claves a la derecha
        for (int i = m + 1; i < tempKeys.size(); i++) {
            rightNode.getData().add(tempKeys.get(i));
        }


        // Hijos del nodo a izquierda
        for (int i = 0; i <= m; i++) {
            Node c = tempChildren.get(i);
            leftNode.getLinks().add(c);
            c.setFather(leftNode);
        }

        // Hijos del nodo a derecha
        for (int i = m + 1; i < tempChildren.size(); i++) {
            Node c = tempChildren.get(i);
            rightNode.getLinks().add(c);
            c.setFather(rightNode);
        }


        //Si el nodo es la raiz, se crea un nuevo para el clave que sube
        if (node.getFather() == null) {
            Node newRoot = new Node();
            newRoot.getData().add(promoted);
            newRoot.getLinks().add(leftNode);
            newRoot.getLinks().add(rightNode);

            leftNode.setFather(newRoot);
            rightNode.setFather(newRoot);

            this.root = newRoot;
            return;
        }


        //Si el nodo no es la raiz
        Node father = node.getFather();

        // Si el padre tiene espacio, se inserta en el padre
        if (father.getData().size() < 2 * father.getOrder()) {
            father.getData().add(promoted);
            father.getData().sort(Comparator.comparing(Tuple::getKey));

            int idx = father.getLinks().indexOf(node);
            father.getLinks().remove(idx);
            father.getLinks().add(idx, leftNode);
            father.getLinks().add(idx + 1, rightNode);

            leftNode.setFather(father);
            rightNode.setFather(father);

        } else {
            // Si el padre no tiene espacio, aplicamos recursividad para dividirlo
            splitInternal(father, promoted, leftNode, rightNode, node);
        }
    }

    private void sheetLinks(Node node, Node nodeLeft, Node nodeRight) {

        Node prev = node.getSheetLinksBack();
        Node next = node.getSheetLinksNext();

        // 1. Enlazar L y R entre sí
        nodeLeft.setSheetLinksNext(nodeRight);
        nodeRight.setSheetLinksBack(nodeLeft);

        // 2. Conectar con el anterior
        if (prev != null) {
            prev.setSheetLinksNext(nodeLeft);
            nodeLeft.setSheetLinksBack(prev);
        }

        // 3. Conectar con el siguiente
        nodeRight.setSheetLinksNext(next);
        if (next != null) {
            next.setSheetLinksBack(nodeRight);
            nodeRight.setSheetLinksNext(next);
        }

        // 4. Limpiar enlaces del nodo viejo
        node.setSheetLinksBack(null);
        node.setSheetLinksNext(null);
    }


    public void insert(Tuple data) {
        insert(data, this.root);
    }


    private Tuple searchId(Long key, Node node) {
        if (node.getLinks().isEmpty()) {

            for (Tuple t : node.getData()) {
                if (t.getKey().equals(key)) {
                    return t;
                }
            }

            return null;
        }

        int count;
        for (count = 0; count < node.getData().size(); count++) {
            if (key < node.getData().get(count).getKey()) {
                break;
            }
        }
        return searchId(key, node.getLinks().get(count));
    }

    public Tuple searchId(Long key) {
        return searchId(key, this.root);
    }

    private List<Tuple> searchCampus(Long key, Node node){
        // Buscar la hoja donde debería empezar
        if (!node.getLinks().isEmpty()) {
            int i = 0;
            while (i < node.getData().size() && key > node.getData().get(i).getKey()) {
                i++;
            }
            return searchCampus(key, node.getLinks().get(i));
        }

        // Estamos en una hoja
        List<Tuple> tuples = new ArrayList<>();
        Node current = node;

        // Recorrer hojas hacia adelante
        while (current != null) {
            for (Tuple t : current.getData()) {
                if (t.getKey().equals(key)) {
                    tuples.add(t);
                }
                else if (t.getKey() > key) {
                    return tuples;
                }
            }
            current = current.getSheetLinksNext();
        }

        return tuples;
    }


    public List<Tuple> searchCampus(Long key) {
        return searchCampus(key, this.root);
    }

   public void printTree() {
        printRec(root, 1);
    }

    private void printRec(Node node, int level) {
        String indent = "    ".repeat(level);

        // Si es nodo hoja
        if (node.getLinks().isEmpty()) {
            System.out.println(indent + "Nivel " + level + " (HOJA): " + printKeys(node));
            return;
        }

        // Nodo interno
        System.out.println(indent + "Nivel " + level + " (INTERNO): " + printKeys(node));

        // Imprimir hijos recursivamente
        for (Node child : node.getLinks()) {
            printRec(child, level + 1);
        }
    }

    // Para mostrar claves como [k1 | k2 | k3]
    private String printKeys(Node node) {
        StringBuilder sb = new StringBuilder("[ ");

        for (Tuple t : node.getData()) {
            sb.append(t.getKey()).append(" | ");
        }

        if (!node.getData().isEmpty()) {
            sb.setLength(sb.length() - 3); // borrar " | "
        }

        sb.append(" ]");
        return sb.toString();
    }


}
