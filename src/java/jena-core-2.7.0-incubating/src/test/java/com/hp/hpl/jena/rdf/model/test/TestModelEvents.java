/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hp.hpl.jena.rdf.model.test;

import com.hp.hpl.jena.rdf.listeners.*;
import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.rdf.model.impl.*;

import java.util.*;
import junit.framework.*;

/**
    Tests for model events and listeners.
 	@author kers
*/
public class TestModelEvents extends ModelTestBase
    {
    public TestModelEvents(String name)
        { super(name); }

    public static TestSuite suite()
        { return new TestSuite( TestModelEvents.class ); }
        
    protected Model model;
    protected RecordingModelListener SL;
    
    @Override public void setUp()
        { 
        model = ModelFactory.createDefaultModel(); 
        SL = new RecordingModelListener();
        }
        
    public void testRegistrationCompiles()
        {
        assertSame( model, model.register( new RecordingModelListener() ) );
        }
        
    public void testUnregistrationCompiles()
        {
        model.unregister( new RecordingModelListener() );
        }
        
    public void testAddSingleStatements()
        {
        Statement S1 = statement( model, "S P O" );
        Statement S2 = statement( model, "A B C" );
        assertFalse( SL.has( new Object [] { "add", S1 } ) );
        model.register( SL );
        model.add( S1 );
        SL.assertHas( new Object[] { "add", S1 } );
        model.add( S2 );
        SL.assertHas( new Object[] { "add", S1, "add", S2 } );
        model.add( S1 );
        SL.assertHas( new Object[] { "add", S1, "add", S2, "add", S1 } );
        }
        
    public void testTwoListeners()
        {
        Statement S = statement( model, "S P O" );
        RecordingModelListener SL1 = new RecordingModelListener();
        RecordingModelListener SL2 = new RecordingModelListener();
        model.register( SL1 ).register( SL2 );
        model.add( S );
        SL2.assertHas( new Object[] { "add", S } );
        SL1.assertHas( new Object[] { "add", S } );
        }
        
    public void testUnregisterWorks()
        {
        model.register( SL );
        model.unregister( SL );
        model.add( statement( model, "X R Y" ) );
        SL.assertHas( new Object[] {} );
        }
        
    public void testRemoveSingleStatements()
        {
        Statement S = statement( model, "D E F" );
        model.register( SL );
        model.add( S );
        model.remove( S );
        SL.assertHas( new Object[] { "add", S, "remove", S } );
        }
        
    public void testAddInPieces()
        {
        model.register( SL );
        model.add( resource( model, "S" ), property( model, "P" ), resource( model, "O" ) );
        SL.assertHas( new Object[] { "add", statement( model, "S P O") } );
        }

    public void testAddStatementArray()
        {
        model.register( SL );
        Statement [] s = statements( model, "a P b; c Q d" );
        model.add( s );
        SL.assertHas( new Object[] {"add[]", Arrays.asList( s )} );
        }
        
    public void testDeleteStatementArray()
        {
        model.register( SL );
        Statement [] s = statements( model, "a P b; c Q d" );
        model.remove( s );
        SL.assertHas( new Object[] {"remove[]", Arrays.asList( s )} );            
        }
        
    public void testAddStatementList()
        {
        model.register( SL );
        List<Statement> L = Arrays.asList( statements( model, "b I g; m U g" ) );
        model.add( L );
        SL.assertHas( new Object[] {"addList", L} );
        }
        
    public void testDeleteStatementList()
        {
        model.register( SL );
        List<Statement> L = Arrays.asList( statements( model, "b I g; m U g" ) );
        model.remove( L );
        SL.assertHas( new Object[] {"removeList", L} );
        }
    
    public void testAddStatementIterator()
        {
        model.register( SL );
        Statement [] sa = statements( model, "x R y; a P b; x R y" );
        StmtIterator it = asIterator( sa );
        model.add( it );
        SL.assertHas( new Object[] {"addIterator", Arrays.asList( sa )} );    
        }
        
    public void testDeleteStatementIterator()
        {
        model.register( SL );
        Statement [] sa = statements( model, "x R y; a P b; x R y" );
        StmtIterator it = asIterator( sa );
        model.remove( it );
        SL.assertHas( new Object[] {"removeIterator", Arrays.asList( sa )} );    
        }
                    
    protected StmtIterator asIterator( Statement [] statements )
        { return new StmtIteratorImpl( Arrays.asList( statements ).iterator() ); }
        
    public void testAddModel()
        {
        model.register( SL );
        Model m = modelWithStatements( "NT beats S; S beats H; H beats D" );
        model.add( m );
        SL.assertHas( new Object[] {"addModel", m} );
        }
        
    public void testDeleteModel()
        {
        model.register( SL );
        Model m = modelWithStatements( "NT beats S; S beats H; H beats D" );
        model.remove( m );
        SL.assertHas( new Object[] {"removeModel", m} );
        }
        
    /**
        Test that the null listener doesn't appear to do anything. Or at least
        doesn't crash ....
    */
    public void testNullListener()
        {
        ModelChangedListener NL = new NullListener();
        model.register( NL );
        model.add( statement( model, "S P O " ) );
        model.remove( statement( model, "X Y Z" ) );
        model.add( statements( model, "a B c; d E f" ) );
        model.remove( statements( model, "g H i; j K l" ) );
        model.add( asIterator( statements( model, "m N o; p Q r" ) ) );
        model.remove( asIterator( statements( model, "s T u; v W x" ) ) );
        model.add( modelWithStatements( "leaves fall softly" ) );
        model.remove( modelWithStatements( "water drips endlessly" ) );
        model.add( Arrays.asList( statements( model, "xx RR yy" ) ) );
        model.remove( Arrays.asList( statements( model, "aa VV rr" ) ) );
        }
        
    public void testChangedListener()
        {
        ChangedListener CL = new ChangedListener();
        model.register( CL );
        assertFalse( CL.hasChanged() );
        model.add( statement( model, "S P O" ) );
        assertTrue( CL.hasChanged() );
        assertFalse( CL.hasChanged() );
        model.remove( statement( model, "ab CD ef" ) );
        assertTrue( CL.hasChanged() );
        model.add( statements( model, "gh IJ kl" ) );
        assertTrue( CL.hasChanged() );
        model.remove( statements( model, "mn OP qr" ) );
        assertTrue( CL.hasChanged() );
        model.add( asIterator( statements( model, "st UV wx" ) ) );
        assertTrue( CL.hasChanged() );
        assertFalse( CL.hasChanged() );
        model.remove( asIterator( statements( model, "yz AB cd" ) ) );
        assertTrue( CL.hasChanged() );
        model.add( modelWithStatements( "ef GH ij" ) );
        assertTrue( CL.hasChanged() );
        model.remove( modelWithStatements( "kl MN op" ) );
        assertTrue( CL.hasChanged() );
        model.add( Arrays.asList( statements( model, "rs TU vw" ) ) );
        assertTrue( CL.hasChanged() );
        model.remove( Arrays.asList( statements( model, "xy wh q" ) ) );
        assertTrue( CL.hasChanged() );
        }
    
    public void testGeneralEvent()
        {
        model.register( SL );
        Object e = new int [] {};
        model.notifyEvent( e );
        SL.assertHas( new Object[] {"someEvent", model, e} ); 
        }

    /**
        Local test class to see that a StatementListener funnels all the changes through
        add/remove a single statement
    */
    public static class WatchStatementListener extends StatementListener
        {
        List<Statement> statements = new ArrayList<Statement>();
        String addOrRem = "<unset>";
        
        public List<Statement> contents()
            { try { return statements; } finally { statements = new ArrayList<Statement>(); } }
        
        public String getAddOrRem()
            { return addOrRem; }
                
        @Override
        public void addedStatement( Statement s )
            { statements.add( s ); addOrRem = "add"; }
            
        @Override
        public void removedStatement( Statement s )
            { statements.add( s ); addOrRem = "rem"; }
        }
        
    public void another( Map<Object, Integer> m, Object x )
        {
        Integer n = m.get( x );
        if (n == null) n = new Integer(0);
        m.put( x, new Integer( n.intValue() + 1 ) ); 
        }
    
    public Map<Object, Integer> asBag( List<Statement> l )
        {
        Map<Object, Integer> result = new HashMap<Object, Integer>();
        for (int i = 0; i < l.size(); i += 1) another( result, l.get(i) );
        return result;    
        }       
        
    public void assertSameBag( List<Statement> wanted, List<Statement> got )
        { assertEquals( asBag( wanted ), asBag( got ) ); }
        
    public void testGot( WatchStatementListener sl, String how, String template )
        {
        assertSameBag( Arrays.asList( statements( model, template ) ), sl.contents() );
        assertEquals( how, sl.getAddOrRem() );
        assertTrue( sl.contents().size() == 0 );
        }
        
    public void testTripleListener()
        {
        WatchStatementListener sl = new WatchStatementListener();    
        model.register( sl );
        model.add( statement( model, "b C d" ) );
        testGot( sl, "add", "b C d" );
        model.remove( statement( model, "e F g" ) );
        testGot( sl, "rem", "e F g" );
    /* */    
        model.add( statements( model, "h I j; k L m" ) );
        testGot( sl, "add", "h I j; k L m" );
        model.remove( statements( model, "n O p; q R s" ) );
        testGot( sl, "rem", "n O p; q R s" );
    /* */    
        model.add( Arrays.asList( statements( model, "t U v; w X y" ) ) );
        testGot( sl, "add", "t U v; w X y" );
        model.remove( Arrays.asList( statements( model, "z A b; c D e" ) ) );
        testGot( sl, "rem", "z A b; c D e" );
    /* */    
        model.add( asIterator( statements( model, "f G h; i J k" ) ) );
        testGot( sl, "add", "f G h; i J k" );
        model.remove( asIterator( statements( model, "l M n; o P q" ) ) );
        testGot( sl, "rem", "l M n; o P q" );
    /* */
        model.add( modelWithStatements( "r S t; u V w; x Y z" ) );
        testGot( sl, "add", "r S t; u V w; x Y z" );
        model.remove( modelWithStatements( "a E i; o U y" ) );
        testGot( sl, "rem", "a E i; o U y" );
        }
        

    static class OL extends ObjectListener
        {
        private Object recorded;
        private String how;
        
        @Override
        public void added( Object x )
            { recorded = x; how = "add"; }
            
        @Override
        public void removed( Object x )
            { recorded = x; how = "rem"; }
        
        private Object comparable( Object x )
            {
            if (x instanceof Statement []) return Arrays.asList( (Statement []) x );
            if (x instanceof Iterator<?>) return iteratorToList( (Iterator<?>) x );
            return x;
            }    
            
        public void recent( String wantHow, Object value ) 
            { 
            assertEquals( comparable( value ), comparable( recorded ) ); 
            assertEquals( wantHow, how );
            recorded = how = null;
            }
        }
        
    public void testObjectListener()
        {
        OL ll = new OL();
        model.register( ll );
        Statement s = statement( model, "aa BB cc" ), s2 = statement( model, "dd EE ff" );
        model.add( s );
        ll.recent( "add", s );
        model.remove( s2 );
        ll.recent( "rem", s2 );
    /* */
        List<Statement> sList = Arrays.asList( statements( model, "gg HH ii; jj KK ll" ) );
        model.add( sList );
        ll.recent( "add", sList );
        List<Statement> sList2 = Arrays.asList( statements( model, "mm NN oo; pp QQ rr; ss TT uu" ) );
        model.remove( sList2 );
        ll.recent( "rem", sList2 );
    /* */
        Model m1 = modelWithStatements( "vv WW xx; yy ZZ aa" );
        model.add( m1 );
        ll.recent( "add", m1 );
        Model m2 = modelWithStatements( "a B g; d E z" );
        model.remove( m2 );
        ll.recent( "rem", m2 );
    /* */
        Statement [] sa1 = statements( model, "th i k; l m n" );
        model.add( sa1 );
        ll.recent( "add", sa1 );
        Statement [] sa2 = statements( model, "x o p; r u ch" );
        model.remove( sa2 );
        ll.recent( "rem", sa2 );
    /* */
        Statement [] si1 = statements( model, "u ph ch; psi om eh" );
        model.add( asIterator( si1 ) );
        ll.recent( "add", asIterator( si1 ) );
        Statement [] si2 = statements( model, "at last the; end of these; tests ok guv" );
        model.remove( asIterator( si2 ) );
        ll.recent( "rem", asIterator( si2 ) );
        }
    }
