package jat.test;
import java.io.IOException;
import java.io.File;
import jat.lang.AtomicFuture;
public class Test extends jat.lang.UnitTest  {
  jat.lang.ValueArray<java.lang.Integer> va =new jat.lang.ValueArray<java.lang.Integer>(new java.lang.Integer[]{1 ,2 ,3 });
  static final int vv =3 ;
  jat.lang.Atom<java.lang.Integer > ai = new jat.lang.Atom</**/java.lang.Integer>();
  jat.lang.Atom<java.lang.Integer > aj =new jat.lang.Atom<java.lang.Integer>(4 );
  jat.lang.Value v =new jat.test.Person();
  int k ;
  int k2 =4 + 2 * (3 - 1 );
  jat.test.Test foo (){
    return this ;
  }
  public Test (){
    /**begin atomic**/
    while(true) {
      try {
        final jat.lang.Transaction __tr__= new jat.lang.Transaction();
        ai.set(3,__tr__);
        __tr__.finish();
        break;
      } catch(jat.lang.TransactionRestart __res__) {
        continue;
      }
    }
    /**end atomic**/
  }
  public void runTest (){
    System .out .println ("Hello, world" );
    for(int i =0 ;i < 10 ;i += 1 ) {
      System .out .println ("i=" + i );
    }
    final int nn =3 + 1 ;
    int n =nn ;
    int i ;
    for(i =0 ;i < n ;i += 1 ) {
      System .out .println ("i=" + i );
    }
    if(n < nn ){
      assertTrue (false ,"n < " + nn );
    }
    else if(jat.lang.Util.equals(n ,nn )){
      System .out .println ("n == " + nn );
    }
    else{
      assertTrue (false ,"n > " + nn );
    }
    while(n > 0 ){
      n -= 1 ;
    }
    assertTrue (jat.lang.Util.equals(n ,0 ),"n == null" );
    jat.test.Test t =new jat.test.Test();
    jat.test.Test t2 =new jat.test.Test().foo ().foo ();
    java.lang.Integer n1 =3 ;
    java.lang.Double n2 =3.0 ;
    assertTrue (jat.lang.Util.equals(n1 ,n2 ),"n1 == n2" );
    java.lang.String s1 ="foo" ;
    java.lang.String s2 =s1 + "" ;
    assertTrue (jat.lang.Util.equals(s1 ,s2 ),"s1 == s2" );
    jat.test.Test test =new jat.test.Test();
    final jat.lang.AtomicFuture<java.lang.Integer> ss =new jat.lang.AtomicFuture<java.lang.Integer>();
    /**begin atomic**/
    while(true) {
      try {
        final jat.lang.Transaction __tr__= new jat.lang.Transaction();
        test.ai.set(test.ai.get(__tr__)+2,__tr__);
        ss.set(test.ai.get(__tr__),__tr__);
        final AtomicFuture<java.lang.String > s = new AtomicFuture<java.lang.String >();
        __tr__.addTask(new Runnable() {
          public void run() {
            s .set(new java.util.concurrent.Callable<java.lang.String >() {
              public java.lang.String call(){
                return "fish" ;
              }
            }.call(),__tr__);
          }
        });
        __tr__.addTask(new Runnable() {
          public void run() {
            System .out .println ("sstran=" + ss .get ());
          }
        });
        __tr__.finish();
        break;
      } catch(jat.lang.TransactionRestart __res__) {
        continue;
      }
    }
    /**end atomic**/
    System .out .println ("ss=" + ss .get ());
    assertTrue (jat.lang.Util.equals(ss .get (),5 ),"ss.get()==5" );
  }
}
