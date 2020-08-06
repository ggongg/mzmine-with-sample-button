/*
 * Copyright 2006-2020 The MZmine Development Team
 *
 * This file is part of MZmine.
 *
 * MZmine is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with MZmine; if not,
 * write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301
 * USA
 */


package io.github.mzmine.modules.dataprocessing.id_cliquems.cliquemsimplementation;

import com.google.common.collect.Range;
import io.github.mzmine.datamodel.IonizationType;
import io.github.mzmine.parameters.parametertypes.tolerances.MZTolerance;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

public class AdductAnnotationCliqueMS {
  // Logger.
  private static final Logger logger = Logger.getLogger(AdductAnnotationCliqueMS.class.getName());

  private AnnotDF readDF(List<PeakData> dfClique){
    AnnotDF annotdf = new AnnotDF();
    for(PeakData pd : dfClique){
      annotdf.mz.add(pd.getMz());
      annotdf.features.add(pd.getNodeID());
      annotdf.charge.add(pd.getCharge());
    }
    return  annotdf;
  }

  private RawAdList readRawList(List<IonizationType> orderadInfo){
    RawAdList rawL = new RawAdList();
    for(IonizationType ion : orderadInfo){
      rawL.rawList.put(ion.getAdductName(),ion);
      rawL.addOrder.add(ion.getAdductName());
    }
    return rawL;
  }

  /**
   * function to sort the list of adducts by score
   * @param rList
   * @return
   */
  private List<Double> getScoreAddList (RawAdList rList){
    List<Double> vScore = new ArrayList<>();
    for(String adKey : rList.rawList.keySet()){
      vScore.add(rList.rawList.get(adKey).getLog10freq());
    }
    Collections.sort(vScore);
    return vScore;
  }

  private HashMap<Integer,String> getAllAdducts(double mass, MZTolerance tol, int idn, IonizationType currentAdd, AnnotDF mzdf, RawAdList rList){
    int idnMass = idn; //index to start the search in the row of adducts
    NavigableMap<Double,String> massMap = new TreeMap<>();
    HashMap<Integer,String> adduMap = new HashMap<>();
    for(String ita : rList.addOrder){
      IonizationType adI = rList.rawList.get(ita);
      double mapmassDiff = -mass + (mass*adI.getNumMol() + adI.getAddedMass())/Math.abs(adI.getCharge());
      massMap.put(mapmassDiff,ita);
    }
    Map.Entry<Double,String> lowerboundp = massMap.firstEntry();
    double lowerbound = lowerboundp.getKey() - lowerboundp.getKey()*0.10;
    Map.Entry<Double,String> upperboundp = massMap.lastEntry();
    double upperbound = upperboundp.getKey() + upperboundp.getKey()*0.10;
    // first see if there is any previous row prior to the one that we start the search
    while((mzdf.mz.get(idnMass) - mass) < lowerbound){
      if(idnMass == 0)
        break;
      idnMass-=1;
    }
    // search for all adducts of the mass in the df
    for(int idnloop = idnMass ; idnloop < mzdf.mz.size() ; idnloop ++){
      Double mzDiff = mzdf.mz.get(idnloop) - mass;
      Double mzVal = mzdf.mz.get(idnloop);
      int isoCharge = mzdf.charge.get(idnloop);
      // if massdifference is bigger than the largest mass difference in the adduct list
      // it is not possible to find more adducts
      NavigableMap<Double,String> subMap = massMap.tailMap(lowerboundp.getKey(),true);
      for(Double itm : subMap.keySet()){
        double error;
        if(isoCharge != 0){
          String addTest = massMap.get(itm);
          IonizationType adITest = rList.rawList.get(addTest);
          if(isoCharge == Math.abs(adITest.getCharge())){
            // if testing add charge is equal to feature charge
            error = Math.abs(mzDiff - itm)/mass;
            // if the error is smaller than the tolerance, accept that adduct
//            if(error < tol*Math.sqrt(2.0))
//              adduMap.put(mzdf.features.get(idnloop),massMap.get(itm));
            MZTolerance modTolerance = new MZTolerance(Math.sqrt(2.0)*tol.getMzTolerance(),Math.sqrt(2.0)*tol.getPpmTolerance());
            Range<Double> tolRange = modTolerance.getToleranceRange(mass);
            if(tolRange.contains(mzVal-itm)){
              // if the error is smaller than the tolerance, accept that adduct
              adduMap.put(mzdf.features.get(idnloop),massMap.get(itm));
            }
          } else{
//            // if not, this annotation is not possible
//            error = 10*tol*Math.sqrt(2.0);
//            // if the error is smaller than the tolerance, accept that adduct
//            if(error < tol*Math.sqrt(2.0))
//              adduMap.put(mzdf.features.get(idnloop),massMap.get(itm));
          }
        } else{
//          // if there is no charge set, proceed as normal
//          error = Math.abs(mzDiff - itm) / mass;
//          // if the error is smaller than the tolerance, accept that adduct
//          if(error < tol*Math.sqrt(2.0))
//            adduMap.put(mzdf.features.get(idnloop),massMap.get(itm));

          // if there is no charge set, proceed as normal
          MZTolerance modTolerance = new MZTolerance(Math.sqrt(2.0)*tol.getMzTolerance(),Math.sqrt(2.0)*tol.getPpmTolerance());
          Range<Double> tolRange = modTolerance.getToleranceRange(mass);
          if(tolRange.contains(mzVal-itm)){
            // if the error is smaller than the tolerance, accept that adduct
            adduMap.put(mzdf.features.get(idnloop),massMap.get(itm));
          }
        }
      }
      // move lowerbound if the mass difference is larger than the lowerbound
      if(mzDiff > lowerboundp.getKey())
        lowerboundp = massMap.higherEntry(lowerboundp.getKey());
      if(mzDiff > upperbound)
        break;
      if(lowerboundp == null)
        break;
    }
    return adduMap;
  }

  private void getComponent(Set<Double> setm, Set<Integer> extraf, AnnotData annotD, Component comp){
    Set<Integer> newf = new HashSet<>();
    for(Integer itf:extraf){
      comp.feature.add(itf); // insert this feature if it is not in the component;
      for(Double itm : annotD.feat2mass.get(itf)){
        // if in this feature there is mass on the setm
        if(setm.contains(itm)){
          // include the mass in the component if it is not yet
          boolean isnew = comp.mass.add(itm);
          // if this mass is new, search for new features
          if(isnew){
            for(Pair<Integer,String> itvm : annotD.massList.get(itm)){
              // if some feature is not in the component feature set, include it in the new features
              if(!comp.feature.contains(itvm.getKey()))
                newf.add(itvm.getKey());
            }
          }
        }
      }
    }
    // if there are no new features to check end
    if(newf.size() > 0)
      getComponent(setm,newf, annotD, comp);
  }

  private void getComponentanG ( Set<Integer> extraf, AnnotData annotD, Component comp ){
    Set<Integer> newf = new HashSet<>();
    for(Integer itf : extraf){
      comp.feature.add(itf);  // insert this feature if it is not in the component
      if(!annotD.feat2mass.containsKey(itf))
        continue;
      for(Double itm : annotD.feat2mass.get(itf)){
        // if in this feature there is mass on the setm
        // include the mass in the component if it is not yet
        boolean isnew = comp.mass.add(itm);
        // if this mass is new, search for new features
        if(isnew){
          for(Pair<Integer,String> itvm : annotD.massList.get(itm)){
            if(!comp.feature.contains(itvm.getKey()))
              newf.add(itvm.getKey());
          }
        }
      }
    }
    if(newf.size() > 0)
      getComponentanG(newf, annotD, comp);
  }

  private HashMap<Integer,Component> getanGcomp(AnnotData annotD){
    HashMap<Integer,Component> mapC = new HashMap<>();
    int id = 0;
    // 1 - include all the features in this clique
    TreeSet<Integer> setf = new TreeSet<>(annotD.features.keySet());
    // 2 - Compute which anGroups exist
    while(setf.size() != 0){
      Component comp = new Component();
      Set<Integer> extraf = new HashSet<>();
      extraf.add(setf.first());
      getComponentanG(extraf, annotD, comp);
      // drop features if comp created
      if(comp.feature.size() > 0) {
        for(Integer itf : comp.feature)
          setf.remove(itf);
        mapC.put(id,comp);
        id++;
      }
    }
    return mapC;
  }

  private boolean compareMasses(AnnotData annotD, double m1, double m2, int anGroup){
    //m1 always is the mass to drop
    boolean result = false;
    int count = 0;
    for(Pair<Integer,String> it1 : annotD.anGroup2mass.get(anGroup).get(m1)){
      for(Pair<Integer,String> it2 : annotD.anGroup2mass.get(anGroup).get(m2)){
        if(it1.equals(it2))
          count++;
      }
    }
    if(count == annotD.anGroup2mass.get(anGroup).get(m1).size())
      result = true;
    return result;
  }


  private Set<Double> getRepMasses(int anGroup, AnnotData annotD, double filter){
    // pair of masses to check, they are ordered from small to big value
    Set<Pair<Double, Double>> badmPair = new HashSet<>();
    for(Double itm1 : annotD.anGroup2mass.get(anGroup).keySet()){
      for(Double itm2 : annotD.anGroup2mass.get(anGroup).keySet()){
        if(!itm1.equals(itm2)){
          double error = Math.abs(itm1 - itm2) / itm1;
          // if the error is smaller than the filter, we have to check the adducts
          if(error < filter* Math.sqrt(2.0)){
            Pair<Double,Double> pairmass;
            if(itm2>itm1){
              pairmass = new Pair(itm1,itm2);
            }
            else{
              pairmass = new Pair(itm2,itm1); //TODO
            }
            badmPair.add(pairmass);
          }
        }
      }
    }
    Set<Double> badmasses = new HashSet<>();
    // now filter the masses that are similar
    for(Pair<Double,Double> itmp : badmPair){
      int size1,size2;
      boolean comp = false;
      size1 = annotD.anGroup2mass.get(anGroup).get(itmp.getKey()).size();
      size2 = annotD.anGroup2mass.get(anGroup).get(itmp.getValue()).size();
      double m1 = itmp.getKey();
      double m2 = itmp.getValue();
      if(size1 == size2){
        // if masses are equal, we eventually drop m1
        comp = compareMasses(annotD,m1,m2,anGroup);
        if(comp){
          badmasses.add(m1);
        }
      }
      else{
        if(size1 > size2){
          // size1 is bigger, drop m2 if conditions met
          comp = compareMasses(annotD, m2, m1, anGroup);
          if(comp)
            badmasses.add(m2);
        }
        else{
          //size 2 is bigger, drop m1 if conditions met
          comp = compareMasses(annotD,m1,m2,anGroup);
          if(comp)
            badmasses.add(m1);
        }
      }
    }
    return badmasses;
  }

  private void createanGroup2mass(AnnotData annotD, HashMap<Integer, Component> anGcomp, double filter){
    // first create anGroup2mass object
    for(Integer itc : anGcomp.keySet()){
      for(Double its : anGcomp.get(itc).mass){
        if(!annotD.anGroup2mass.containsKey(itc)){
          HashMap <Double, List<Pair<Integer,String>>> tempHash = new HashMap();
          tempHash.put(its,annotD.massList.get(its));
          annotD.anGroup2mass.put(itc,tempHash);
        }
        else{
          annotD.anGroup2mass.get(itc).put(its,annotD.massList.get(its));
        }
      }
    }
    // second filter masses with very similar mass and the same adducts, or less adducts in one case
    for(Integer itg : annotD.anGroup2mass.keySet()){
      Set<Double> setm = getRepMasses(itg, annotD, filter);
      // if there are repeated masses drop these masses
      if(setm.size() > 0){
        for(Double itms : setm){
          annotD.anGroup2mass.get(itg).remove(itms);
          annotD.massList.remove(itms); // also drop them in the massList
          for(Integer itf : annotD.anGroups.get(itg)){
            List<Double> tempL = annotD.feat2mass.get(itf);
            tempL.remove(itms); // and drop them from the mass assigned to each feature
          }
        }
      }
    }
  }


  private AnnotData getAnnotData(RawAdList rList, AnnotDF mzdf, MZTolerance tol, double filter){
    AnnotData annotD = new AnnotData();
    for(Integer f : mzdf.features)
      annotD.features.put(f,-1);
    for(int idn = 0 ; idn < (mzdf.mz.size() -1) ; idn++){
      double mz;
      Integer isoCharge;
      mz = mzdf.mz.get(idn); // assign m/z value for current idn position
      isoCharge = mzdf.charge.get(idn); // assign charge value, only available for isotopic features
      for(String ita : rList.addOrder){
        double mass;
        IonizationType currentAdd = rList.rawList.get(ita);
        if(!isoCharge.equals(0)) {
          // if mz is an isotopic feature with assigned charge
          mass = -1.0;
          if (Math.abs(currentAdd.getCharge()) == isoCharge) {
            // only annotate adduct if the charge of the isotopic feature is equal to the charge of the putative adduct
            mass = (mz * Math.abs(currentAdd.getCharge()) - currentAdd.getAddedMass()) / currentAdd.getNumMol();
          }
        } else{
          mass = (mz * Math.abs(currentAdd.getCharge()) - currentAdd.getAddedMass()) / currentAdd.getNumMol();
        }
        if(mass > 0){
          mass = Math.round(mass*1000)/1000.0;
          if(!annotD.massList.containsKey(mass)){
            // if this mass is not on the mass list, search for all the adducts of that mass
            HashMap<Integer,String> adduMap = getAllAdducts(mass, tol, idn, currentAdd, mzdf, rList);
            // if there is more than one adduct:
            if(adduMap.size() > 1){
              for(Integer itu : adduMap.keySet()){
                // update massList
                Pair<Integer,String> massPair = new Pair(itu,adduMap.get(itu));
                if(annotD.massList.containsKey(mass)){
                  annotD.massList.get(mass).add(massPair);
                }
                else{
                  List<Pair<Integer,String>> temp = new ArrayList<>();
                  temp.add(massPair);
                  annotD.massList.put(mass,temp);
                }
                // update at which features is this mass
                if(annotD.feat2mass.containsKey(itu)){
                  annotD.feat2mass.get(itu).add(mass);
                }
                else{
                  List<Double> temp = new ArrayList<>();
                  temp.add(mass);
                  annotD.feat2mass.put(itu,temp);
                }
              }
            }
          }
        }
      }

    }
    //create anGroups
    HashMap<Integer, Component> anGcomp = getanGcomp(annotD);
    for(Integer itc : anGcomp.keySet()){
      for(Integer itf : anGcomp.get(itc).feature){
        if(annotD.anGroups.containsKey(itc)){
          annotD.anGroups.get(itc).add(itf);
        }
        else{
          List<Integer> tempList = new ArrayList<>();
          tempList.add(itf);
          annotD.anGroups.put(itc,tempList);
        }
        annotD.features.put(itf,itc);
      }
    }
    for(Integer f: annotD.features.keySet()){
      if(!annotD.feat2mass.containsKey(f)){
        List<Double> tempMass = new ArrayList<>();
        annotD.feat2mass.put(f,tempMass);
      }
    }

    // create and filter angroup2mass
    createanGroup2mass(annotD, anGcomp, filter);


    return annotD;
  }

  private List<Pair<Double,Double>> sortMass (AnnotData annotD, int feature, HashMap<Double, Pair<Double, Double>> mass2score, int n){
    List<Pair<Double,Double>> topV = new ArrayList<>();
    List<Pair<Double,Double>> allM = new ArrayList<>();

    if(annotD.feat2mass.containsKey(feature)){
      if(annotD.feat2mass.containsKey(feature)){
      for(Double itm : annotD.feat2mass.get(feature)){
        if(mass2score.get(itm) != null)
          allM.add(mass2score.get(itm));
        }
      }
    }


    // sort mass vector according to score
    allM.sort((o1,o2) -> Double.compare(o2.getKey(),o1.getKey()));
    // select the top "n" masses
    for(int id = 0 ; id < n ; id++){
      if(id < allM.size())
        // not add more masses in case that for that feature are less than "n" top masses
        topV.add(allM.get(id));
    }
    return topV;
  }

  private HashSet<Double> getTopScoringMasses(AnnotData annotD, int anG, RawAdList rList,int nfeature, int ntotal, double emptyS){
    // we want to get the "n" top scoring masses for each feature of the annotationGroup
    HashSet<Double> setm = new HashSet<>();
    List<Pair<Double,Double>> allM = new ArrayList<>();
    List<Pair<Double,Double>> topT = new ArrayList<>();
    HashMap<Double,Pair<Double,Double>> mass2score = new HashMap<>();

    // First compute the score for all the masses in the anGroup
    if(annotD.anGroup2mass.containsKey(anG)){
      for(Double itm : annotD.anGroup2mass.get(anG).keySet()){
        double score = 0;
        for(Pair<Integer, String> ita  : annotD.anGroup2mass.get(anG).get(itm)){
          IonizationType adI = rList.rawList.get(ita.getValue());
          score += adI.getLog10freq();  // compute the score according to the log frequence of each adduct in the adduct list
        }
        //add the compensation for empty annotations
        score += emptyS * (annotD.anGroups.get(anG).size() - annotD.anGroup2mass.get(anG).get(itm).size());
        Pair<Double,Double> mass2scoreEntry = new Pair(score,itm);
        mass2score.put(itm,mass2scoreEntry);
      }
    }
    // Second select the top masses independently of the feature for that group
    for(Double itm1 : mass2score.keySet())
      allM.add(mass2score.get(itm1));
    allM.sort((o1,o2) -> Double.compare(o2.getKey(),o1.getKey()));  // sort this masses in descending order

    for(int id = 0 ; id < ntotal ; id++){
      if(id < allM.size()) // not add more masses in case that there are less than "n" top masses
        topT.add(allM.get(id));
    }
    // Add this masses to the total set of masses
    for(Pair<Double,Double> itv : topT)
      setm.add(itv.getValue());

    // Third, for each feature, select the "n" top scoring masses
    for(Integer itf : annotD.anGroups.get(anG)){
      List<Pair<Double,Double>> topF = sortMass(annotD, itf, mass2score, nfeature);
      for(Pair<Double,Double> itv : topF){
        setm.add(itv.getValue());
      }
    }
    return setm;
  }

  private Annotation annotateMass(AnnotData annotD, HashSet<Integer> features, RawAdList rList, HashSet<Double> setm, double emptyS){
    Annotation an = new Annotation();
    an.score = 0.0;
    List<Pair<Double,Double>> mass2score = new ArrayList<>(); // first is the score, second is the mass
    HashSet<Integer> freef = new HashSet<>();
    // 1 - for each mass in setm compute the score for the features if that features are more than in two positions
    for(Double itm : setm){
      double score = 0.0;
      int count = 0;
      for(Integer itf : features){
        //check if this feature is in the massList
        for(Pair<Integer,String> itp : annotD.massList.get(itm)){
          // if this mass contains the feature itf, compute the score
          if(itp.getKey().equals(itf)){
            IonizationType adI = rList.rawList.get(itp.getValue());
            score+= adI.getLog10freq();
            count++;
          }
        }
      }
      if(count > 1){
        Pair<Double,Double> mass2scoreEntry = new Pair(score,itm);
        mass2score.add(mass2scoreEntry);
      }
    }
    // if there is no annotation for the features, return an empty score
    if(mass2score.size()  < 1){
      an.score = emptyS * features.size();
      for(Integer itf : features){
        Pair<Double, String> anEntry = new Pair(0.0,"");
        an.annotation.put(itf,anEntry);
      }
      return an;
    }
    // 2 - sort annotation and select the adducts of that annotation
    mass2score.sort((o1,o2) -> Double.compare(o2.getKey(),o1.getKey()));
    double topmass = mass2score.get(0).getValue();
    an.score = mass2score.get(0).getKey();
    an.score -= 10.0; // add the log compensation of -10 for each new mass

    // search again for adduct in that feature and include in annotation
    for(Integer itf : features){
      for(Pair<Integer,String> itp : annotD.massList.get(topmass)){
        // if this mass contains the feature itf, include it in the annotation
        if(itp.getKey().equals(itf)){
          Pair<Double, String> anEntry = new Pair(topmass,itp.getValue());
          an.annotation.put(itf,anEntry); // mass and adduct
        }
      }
    }
    // now put the features out of the annotation in the freef
    for(Integer itf : features){
      if(!an.annotation.containsKey(itf)){
        freef.add(itf);
      }
    }
    if(freef.size() > 1){
      setm.remove(topmass);
      Annotation anfree = annotateMass(annotD, freef, rList, setm, emptyS);
      for(Integer itan : anfree.annotation.keySet()){
        // add the additional score and annotation after the recursive call
        an.annotation.put(itan,anfree.annotation.get(itan));
      }
      an.score += anfree.score;
    }
    else{
      if(freef.size() == 1){
        an.score += emptyS;
        List<Integer> temp = new ArrayList<>(freef);
        Pair<Double, String> anEntry = new Pair(0.0,"");
        an.annotation.put(temp.get(0),anEntry);
      }
    }
    return an;
  }

  private boolean compareAnnotation(int id1, int id2, HashMap<Integer, Annotation> annotations){
    boolean result = false;
    int count = annotations.get(id1).annotation.size();
    for(Integer itf : annotations.get(id1).annotation.keySet()){
      // one less for the count if the annotation for that feature is the same
      if(annotations.get(id1).annotation.get(itf).equals(annotations.get(id2).annotation.get(itf)))
        count--;
    }
    // if all annotations are the same, drop than annotation
    if(count == 0)
      result = true;
    return result;
  }

  private void dropRepeatedAnnotations(HashMap<Integer,Annotation> annotations){
    HashSet<Pair<Integer,Integer>> badA = new HashSet<>();
    HashSet<Integer> dropA = new HashSet<>();
    for(Integer itm1 : annotations.keySet()){
      double score1 = annotations.get(itm1).score;
      for(Integer itm2 : annotations.keySet()){
        // if is not the same annotation
        if(!itm1.equals(itm2)){
          double score2 = annotations.get(itm2).score;
          if(score1 == score2){
            // if itm1->first annotation is bigger
            if(itm1 > itm2){
              Pair<Integer, Integer> badAEntry = new Pair(itm2,itm1);
              badA.add(badAEntry);
            } else{
              Pair<Integer, Integer> badAEntry = new Pair(itm1,itm2);
              badA.add(badAEntry);
            }
          }
        }
      }
    }
    for(Pair<Integer,Integer> itp : badA){
      int id1 = itp.getKey();
      int id2 = itp.getValue();
      boolean compareA = compareAnnotation(id1, id2, annotations);
      // if the two annotations are exactly the same, drop the annotation with a bigger id
      if(compareA)
        dropA.add(id2);
    }
    // if there are equal annotations, drop them from annotations map
    if(dropA.size()  > 0){
      for(Integer its : dropA)
        annotations.remove(its);
    }
  }

  private HashMap<Integer, Annotation> annotateMassLoop(AnnotData annotD, Set<Integer> features, RawAdList rList, Set<Double> setm, double emptyS){
    HashMap<Integer, Annotation> annotations = new HashMap<>();
    int id = 0;
    // 1 - For each mass get the annotations that coincide with features
    for(Double itm : setm){
      HashSet<Integer> freef = new HashSet<>();
      HashSet<Double> setm2 = new HashSet<>(setm);
      Annotation an = new Annotation();
      an.score = 0.0;
      for(Integer itf : features){
        for(Pair<Integer,String> itp : annotD.massList.get(itm)){
          // if this mass contains the feature itf, include it in the annotation
          if(itp.getKey().equals(itf)){
            Pair<Double, String> anEntry = new Pair(itm,itp.getValue());
            an.annotation.put(itf, anEntry); // mass and adduct
            IonizationType adI = rList.rawList.get(itp.getValue());
            an.score += adI.getLog10freq();
          }
        }
      }
      // 2 - Put the features out of the annotation of this mass *itm in the freef
      for(Integer itf : features){
        if(!an.annotation.containsKey(itf))
          freef.add(itf);
      }

      // 3 - Execute AnnotateMass with the freef features
      if(freef.size() > 1){
        setm2.remove(itm);
        Annotation anfree = annotateMass(annotD, freef, rList, setm2, emptyS);
        for(Integer itan : anfree.annotation.keySet()){
          an.annotation.put(itan,anfree.annotation.get(itan));
        }
        an.score += anfree.score;
      } else {
        if( freef.size() == 1){
          an.score += emptyS;
          List<Integer> tempL = new ArrayList<>(freef);
          Pair<Double, String> anEntry = new Pair(0.0,"");
          an.annotation.put(tempL.get(0), anEntry);
        }
      }
      // 4 - Include this annotation in the map
      annotations.put(id,an);
      id++;
    }
    // 5 - Filter equal annotations
    dropRepeatedAnnotations(annotations);
    return annotations;
  }

  private List<Integer> sortAnnotation (HashMap<Integer, Annotation> annotations, int top){
    List<Pair<Double, Integer>> allAn = new ArrayList<>();
    List<Integer> topAn = new ArrayList<>();

    for(Integer ita : annotations.keySet()){
      Pair<Double, Integer> allAnEntry = new Pair(annotations.get(ita).score, ita);
      allAn.add(allAnEntry);
    }
    allAn.sort((o1,o2)->Double.compare(o2.getKey(),o1.getKey()));
    for(int id = 0; id < top ; id++){
      if(id < allAn.size())
        topAn.add(allAn.get(id).getValue());
    }
    return topAn;
  }

  private Double computeMaxScore(List<Double> vScore, int annotsize, double newmass) {
    double score = 0;
    double completeroundscore=0.0,remainderroundscore = 0.0;
    int completeround = annotsize / vScore.size();
    int remainderround = annotsize % vScore.size();
    // compute score by number of complete rounds
    for(Double ritv : vScore)
      completeroundscore += ritv;

    int ritv = vScore.size()-1;
    for(int i = 0 ; i < remainderround ; i++){
      remainderroundscore += vScore.get(ritv);
      ritv--;
    }
    //the final score is the number of loops with the total list, plus the number of extramasses, and the remainder adduct not complete
    score = (completeround * completeroundscore) + remainderroundscore + (completeround * newmass);
    return score;
  }

  private void normalizeAnnotation(Annotation an, List<Double> vScore, double newmass, double emptyS, int newmassSize ){
    double maxscore=0.0, minscore=0.0, oldscore=0.0, newscore = 0.0;
    oldscore = an.score;
    maxscore = computeMaxScore(vScore, an.annotation.size(), newmass);
    // computation of min score is as if all the annotation is empty score
    // plus a compensation of a new mass each 8 features
    minscore = an.annotation.size()*emptyS + newmass*((double)an.annotation.size()/newmassSize);
    newscore = 100*(oldscore - minscore)/(maxscore - minscore); // taken from the linear interpolation formula
    if(newscore < 0) // there are cases where new score is below 0, in those cases the normalized score should be scaled to zero
      newscore = 0;
    an.score = Math.round(10000*(newscore))/10000.0;
  }

  private HashMap<Integer, Component> getSeparateComp(HashSet<Double> setm, AnnotData annotD, int anG){
    HashMap<Integer, Component> mapC = new HashMap<>();
    int id = 0;
    HashSet<Integer> setf = new HashSet<>();
    // 1 - transform the vector of features in a set
    for(Integer itv : annotD.anGroups.get(anG))
      setf.add(itv);
    //2 - compute Components
    while(setf.size() != 0){
      Component comp = new Component();
      HashSet<Integer> extraf = new HashSet<>();
      Integer it = null;
      for(Integer x : setf){
        it = x;
        break; // get an element of the set
      }
      extraf.add(it);
      getComponent(setm, extraf, annotD, comp);
      // drop features if comp created
      if(comp.feature.size() > 0){
        for(Integer itf : comp.feature)
          setf.remove(itf);
        mapC.put(id,comp);
        id++;
      }
    }
    return mapC;
  }



  public OutputAn returnAdductAnnotation(List<PeakData> dfClique,List<IonizationType>  mzdf, int topmassf, int topmasstotal, int sizeanG, MZTolerance tol, double filter, double emptyS, boolean normalizeScore){
    double newMass = -10.0;
    int defaultNewMassSize = 8;
    // 1 - read ordered data frame of features and masses from PeakData
    AnnotDF annotdf = readDF(dfClique);
    // 2 - read data frame with adduct list and adduct information from orderadinfo
    RawAdList rList = readRawList(mzdf);
    List<Double> vScore = getScoreAddList(rList);
    // 3 - obtain all adducts and mass candidates
    AnnotData annotD = getAnnotData(rList, annotdf, tol, filter);
    // 4 - create an object for putting the results of annotation
    OutputAn outAn = new OutputAn(annotdf.features);
    // 5 - find annotation for all annotation groups in this clique
    for(Integer itg : annotD.anGroups.keySet()){
      HashSet<Double> setm = getTopScoringMasses(annotD, itg, rList, topmassf, topmasstotal, emptyS);
      if(annotD.anGroups.get(itg).size() > sizeanG){
        // in case that there are a lot of features in this annotation group, separate components
        HashMap<Integer, Component> components = getSeparateComp(setm, annotD, itg);
        // get all annotations
        for(Integer itc : components.keySet()){
          HashMap<Integer,Annotation> annotations = annotateMassLoop(annotD, components.get(itc).feature, rList, components.get(itc).mass, emptyS);
          // get the id of the top five annotations
          List<Integer> topAn = sortAnnotation(annotations, 5);
          // 6 - Now put this annotations in the output object
          for(int x = 0 ; x<5 ; x++){
            if(topAn.size() > x){
              // normalize the scores
              if(normalizeScore && x==0){
                for(Integer itv : topAn)
                  normalizeAnnotation(annotations.get(itv), vScore, newMass, emptyS, defaultNewMassSize);
              }
              // annotation i
              for(Integer ita1 : annotations.get(topAn.get(x)).annotation.keySet()){
                outAn.scores.get(x).put(ita1, annotations.get(topAn.get(x)).score);
                outAn.ans.get(x).put(ita1,annotations.get(topAn.get(x)).annotation.get(ita1).getValue());
                outAn.masses.get(x).put(ita1,annotations.get(topAn.get(x)).annotation.get(ita1).getKey());
              }
            }
          }
        }
      } else{
        // not necessary to separate in components, annotate all features in the anGroup
        Set<Integer> setf = new HashSet<>();
        for(Integer itgf : annotD.anGroups.get(itg))
          setf.add(itgf);
        HashMap<Integer, Annotation> annotations = annotateMassLoop(annotD, setf, rList, setm, emptyS);
        for(Integer i : annotations.keySet())
        for(Integer itex1 : annotations.keySet()){
          for(Integer itex2 : annotations.get(itex1).annotation.keySet()){
            //TODO
          }
        }
        List<Integer> topAn = sortAnnotation(annotations, 5);
        for(int x=0; x<5; x++){
          if(topAn.size() > x){
            if(normalizeScore && x==0){
              for(Integer itv : topAn)
                normalizeAnnotation(annotations.get(itv), vScore, newMass, emptyS, defaultNewMassSize);
            }
            //annotation x
            for(Integer ita1 : annotations.get(topAn.get(x)).annotation.keySet()){
              outAn.scores.get(x).put(ita1, annotations.get(topAn.get(x)).score);
              outAn.ans.get(x).put(ita1, annotations.get(topAn.get(x)).annotation.get(ita1).getValue());
              outAn.masses.get(x).put(ita1, annotations.get(topAn.get(x)).annotation.get(ita1).getKey());
            }
          }
        }
        setf.clear();
      }
    }
    return outAn;
  }

}

class AnnotDF {
  List<Double> mz = new ArrayList<>();
  List<Integer> features = new ArrayList<>();
  List<Integer> charge = new ArrayList<>();
}


class RawAdList {
  HashMap<String,IonizationType> rawList = new HashMap<>();
  List<String> addOrder = new ArrayList<>();
}

class AnnotData{
  HashMap<Integer,Integer> features = new HashMap<>();
  HashMap<Double,List<Pair<Integer,String>>> massList = new HashMap<>();
  HashMap<Integer, List<Double>> feat2mass = new HashMap<>();
  HashMap<Integer, List<Integer>> anGroups = new HashMap<>();
  HashMap<Integer,HashMap<Double,List<Pair<Integer,String>>>> anGroup2mass = new HashMap<>();
}

class Component {
  Set<Double> mass = new HashSet<>();
  Set<Integer> feature = new HashSet<>();
}

class Annotation {
  Double score = 0.0;
  HashMap<Integer,Pair<Double,String>> annotation = new HashMap<>();
}