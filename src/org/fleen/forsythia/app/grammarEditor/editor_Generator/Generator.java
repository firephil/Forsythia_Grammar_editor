package org.fleen.forsythia.app.grammarEditor.editor_Generator;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import org.fleen.forsythia.app.grammarEditor.GE;
import org.fleen.forsythia.app.grammarEditor.editor_Generator.ui.PanViewer;
import org.fleen.forsythia.app.grammarEditor.editor_Generator.ui.UI_Generator;
import org.fleen.forsythia.core.composition.ForsythiaComposition;

public class Generator implements Serializable{
  
  private static final long serialVersionUID=-4830057832022895569L;

  /*
   * ################################
   * STOP AND GO
   * main generator switch
   * stop generating or go generating
   * ################################
   */
  
  private static final boolean 
    STOPGO_STOP=false,
    STOPGO_GO=true;
  
  private boolean stopgo=STOPGO_STOP;
  
  void toggleStopGo(){
    if(isStop())
      go();
    else
      stop();}
  
  public void stop(){
    stopgo=STOPGO_STOP;}
  
  public void go(){
    stopgo=STOPGO_GO;
    requestgeneratecomposition=true;}
  
  boolean isStop(){
    return stopgo==STOPGO_STOP;}
  
  boolean isGo(){
    return stopgo==STOPGO_GO;}
  
  /*
   * ################################
   * MODE
   * we have 2 generation modes
   * continuous
   *   every foo milliseconds generate and display a new composition
   * intermittant
   *   at invocation of toggleStopGo(), toggling from stop to go, we 
   *   create and display a composition and then we toggle stopgo back to stop.
   * ################################
   */
  
  private static final boolean 
    MODE_INTERMITTANT=false,
    MODE_CONTINUOUS=true;
  
  private boolean mode=MODE_INTERMITTANT;
  
  void toggleMode(){
    mode=!mode;}
  
  void setModeIntermittant(){
    mode=MODE_INTERMITTANT;}
  
  void setModeContinuous(){
    mode=MODE_CONTINUOUS;}
  
  boolean isIntermittant(){
    return mode==MODE_INTERMITTANT;}
  
  boolean isContinuous(){
    return mode==MODE_CONTINUOUS;}
  
  /*
   * ################################
   * CONTINUOUS MODE INTERVAL
   * In continuous mode we have a time interval between compositions
   * it's a number of milliseconds
   * ################################
   */
  private static final long 
    INTERVAL_MAX=999999,
    INTERVAL_MIN=100,
    INTERVAL_DEFAULT=1000;
  
  private long interval=INTERVAL_DEFAULT;
  
  void setInterval(long i){
    if(i<INTERVAL_MIN)i=INTERVAL_MIN;
    if(i>INTERVAL_MAX)i=INTERVAL_MAX;
    interval=i;}
  
  long getInterval(){
    return interval;}
  
  /*
   * ################################
   * COMPOSITION DETAIL FLOOR
   * The floor on smallest polygon detail size. 
   * A polygon with detail size smaller than this does not get cultivated.
   * This value controls detail level in our compositions
   * ################################  
   */
  
  private static final double 
    DETAILFLOOR_DEFAULT=0.05,
    DETAILFLOOR_MIN=0.006,
    DETAILFLOOR_MAX=0.8;
  
  private double detailfloor=DETAILFLOOR_DEFAULT;
  
  void setDetailFloor(double f){
    if(f<DETAILFLOOR_MIN)f=DETAILFLOOR_MIN;
    if(f>DETAILFLOOR_MAX)f=DETAILFLOOR_MAX;
    detailfloor=f;}
  
  double getDetailFloor(){
    return detailfloor;}
  
  /*
   * ################################
   * CONTROL THREAD
   * every CONTROLCHECKINTERVAL ms
   *   check everything
   *   do something if necessary
   * ################################
   */
  
  private static final long CONTROLCHECKINTERVAL=200;
  private boolean 
    runcontrolthread,
    requestgeneratecomposition=false;
  private long compositiongenerationtime=-1;
  
  public void startControlThread(){
    runcontrolthread=true;
    new Thread(){
      public void run(){
        while(runcontrolthread){
          //
          if(isGo()){
            if(isIntermittant()){
              if(requestgeneratecomposition){
                requestgeneratecomposition=false;
                generateComposition();
                renderCompositionForViewer();
                stopgo=STOPGO_STOP;
                GE.ge.editor_generator.refreshUI();}
            }else{//isContinuous()
              if(timeToGenerateAnotherCompositionForContinuous()){
                compositiongenerationtime=System.currentTimeMillis();
                generateComposition();
                renderCompositionForViewer();
                GE.ge.editor_generator.refreshInfo();
                GE.ge.editor_generator.refreshViewer();}}}
          //sleep periodically
          try{
            Thread.sleep(CONTROLCHECKINTERVAL,0);
          }catch(Exception x){
            x.printStackTrace();}}}}.start();}
  
  public void stopControlThread(){
    runcontrolthread=false;}
  
  private boolean timeToGenerateAnotherCompositionForContinuous(){
    long t=System.currentTimeMillis();
    return (t-compositiongenerationtime)>interval;}
  
  /*
   * ################################
   * COMPOSER
   * ################################
   */
  
  private transient Composer composer=null;
  
  public Composer getComposer(){
    if(composer==null)
      composer=new Composer();
    return composer;}
  
  private void resetComposer(){
    composer=new Composer();}
  
  /*
   * ################################
   * RENDERER
   * ################################
   */
  
  private transient Renderer renderer=null;
  
  public Renderer getRenderer(){
    if(renderer==null)
      renderer=new Renderer();
    return renderer;}
  
  private void resetRenderer(){
    renderer=new Renderer();}
  
  /*
   * ################################
   * GENERATE COMPOSITION
   * ################################
   */
  
  public transient ForsythiaComposition composition=null;
  
  private void generateComposition(){
    System.out.println("generate composition");
    resetComposer();
    composition=getComposer().compose(GE.ge.focusgrammar.getForsythiaGrammar(),detailfloor);}
  
  /*
   * ################################
   * RENDER COMPOSITION FOR VIEWER
   * ################################
   */
  
  public transient BufferedImage viewerimage=null;
  
  private void renderCompositionForViewer(){
    System.out.println("render composition for viewer");
    PanViewer viewer=((UI_Generator)GE.ge.editor_generator.getUI()).panviewer;
    resetRenderer();
    viewerimage=getRenderer().getImage(viewer.getWidth(),viewer.getHeight(),composition);}
  
  /*
   * ################################
   * RENDER COMPOSITION FOR EXPORT
   * ################################
   */
  
  public BufferedImage renderCompositionForImageExport(int imagesize){
    int 
      iwidth,
      iheight;
    Rectangle2D.Double bounds=composition.getRootPolygon().getDPolygon().getBounds();
    double
      cwidth=bounds.getWidth(),
      cheight=bounds.getHeight();
    if(cwidth>cheight){
      iwidth=imagesize;
      iheight=(int)((cheight/cwidth)*imagesize);
    }else{
      iheight=imagesize;
      iwidth=(int)((cwidth/cheight)*imagesize);}
    //
    return getRenderer().getImage(iwidth,iheight,composition);}
  
}
