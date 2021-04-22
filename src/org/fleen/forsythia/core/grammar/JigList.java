/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.fleen.forsythia.core.grammar;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.fleen.forsythia.core.Forsythia;
import org.fleen.forsythia.core.composition.FPolygon;
import org.fleen.forsythia.core.grammar.Jig;
import org.fleen.forsythia.core.grammar.JigList;




public class JigList extends ArrayList<Jig> implements Forsythia {
  private static final long serialVersionUID = 866462351907868545L;
  
  public JigList(Collection<Jig> jigs) {
    super(jigs.size());
    init(jigs);
  }
  
class DetailSizeComparator implements Comparator<Jig> {
  private DetailSizeComparator() {}
  
  public int compare(Jig j0, Jig j1) {
    double bds0 = j0.getDetailSizePreviewBaseDetailSize();
    double bds1 = j1.getDetailSizePreviewBaseDetailSize();
    if (bds0 == bds1)
      return 0; 
    if (bds0 > bds1)
      return 1; 
    return -1;
  }
}
  private void init(Collection<Jig> jigs) {
    addAll(jigs);
    Collections.sort(this, (Comparator<Jig>)new DetailSizeComparator( ));
  }
  
  public List<Jig> getJigsAboveDetailSizeFloor(FPolygon target, double floor) {
    List<Jig> jigs = new ArrayList<>();
    for (int i = size() - 1; i > -1; i--) {
      Jig jig = (Jig)get(i);
      if (jig.getDetailSizePreview(target) < floor)
        break; 
      jigs.add(jig);
    } 
    return jigs;
  }
  
  
  public List<Jig> getJigsAboveDetailSizeFloorWithTags(FPolygon target, double floor, String[] tags) {
    List<Jig> a = getJigsAboveDetailSizeFloor(target, floor);
    List<Jig> b = new ArrayList<>();
    for (Jig j : a) {
      if (j.hasTags(tags))
        b.add(j); 
    } 
    return b;
  }

  public String toString() {
    return "[" + getClass().getSimpleName() + " " + "size=" + size() + "]";
  }
}
