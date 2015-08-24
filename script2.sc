



/********************************************************/
/*                                                      */
/* Our first Optimization 'compile' is intended to      */
/* produce a design that will meet hold-time            */
/* under worst-case conditions:                         */
/* - slowest process corner                             */
/* - highest operating temperature and lowest Vcc       */
/* - expected worst case clock skew                     */
/*                                                      */
/********************************************************/

/*------------------------------------------------------*/
/* Specify the worst case (slowest) libraries and       */
/* The library has not been characterised    	      	*/
/* for  Operating conditions. 	             	        */
/*------------------------------------------------------*/

target_library = {"ncsulib25_worst.db"} 
link_library = {"ncsulib25_worst.db"} 

/*------------------------------------------------------*/
/* Specify a 5000 ps clock period with 50% duty cycle     */
/* and a skew of 300 ps                                 */
/*------------------------------------------------------*/

Create_clock -period 6700 -waveform {0 3350} clock
set_clock_skew  -uncertainty 300 clock

/********************************************************/
/*                                                      */
/* Now set up the 'CONSTRAINTS' on the design:          */
/* 1.  How much of the clock period is lost in the      */
/*     modules connected to it                          */
/* 2.  What type of cells are driving the inputs        */
/* 3.  What type of cells and how many (fanout) must it */
/*     be able to drive                                 */
/*                                                      */
/********************************************************/

/*------------------------------------------------------*/
/* ASSUME being driven by a slowest D-flip-flop         */
/* The DFF cell has a worst clock-Q delay of 900 ps     */
/* Allow another 200 ps for wiring delay                */
/* NOTE: THESE ARE INITIAL ASSUMPTIONS ONLY             */
/*------------------------------------------------------*/

set_input_delay 1100 -clock clock all_inputs() - clock


/*------------------------------------------------------*/
/* ASSUME this module is driving a D-flip-flip          */
/* The DFF cell has a worst set-up time of 750 ps       */
/* Allow another 200 ps for wiring delay                */
/* NOTE: THESE ARE INITIAL ASSUMPTIONS ONLY             */
/*------------------------------------------------------*/

set_output_delay 950 -clock clock all_outputs()

/*------------------------------------------------------*/
/* ASSUME being driven by a D-flip-flop                 */
/*------------------------------------------------------*/

set_driving_cell -cell "dp_2" -pin "q" all_inputs() - clock

/*------------------------------------------------------*/
/* ASSUME the worst case output load is                 */
/* 3 D-flip-flop (D-inputs) and                         */
/* and 0.5 units of wiring capacitance                  */
/*------------------------------------------------------*/

port_load = 0.5 + 3 *  load_of (ncsulib25_worst/dp_2/ip) 
set_load port_load all_outputs()           	      	     

/********************************************************/
/*                                                      */
/* Now set the GOALS for the compile                    */
/*                                                      */
/* In most cases you want minimum area, so set the      */
/* goal for maximum area to be 0                        */
/*                                                      */
/********************************************************/

set_max_area 0

/*------------------------------------------------------*/
/* During the initial map (synthesis), Synopsys might   */
/* have built parts (such as adders) using its          */
/* DesignWare(TM) library.  In order to remap the       */
/* design to our TSMC025 library AND to create scope    */
/* for logic reduction, I want to 'flatten out' the     */
/* DesignWare components.  i.e. Make one flat design    */
/*                                                      */
/* 'replace_synthetic' is the cleanest way of doing this*/
/*------------------------------------------------------*/

replace_synthetic -ungroup

/*------------------------------------------------------*/
/* check the design before optimization                 */
/*------------------------------------------------------*/
check_design
check_timing

/********************************************************/
/*                                                      */
/* Now resynthesize the design to meet constraints,     */
/* and try to best achieve the goal, and using the      */
/* CMOSX parts.  In large designs, compile can take     */
/* a lllooonnnnggg  time                                */
/*                                                      */
/********************************************************/

/*------------------------------------------------------*/
/* -map_effort specifies how much optimization effort   */
/*      there is low, medium, and high                  */
/*      use high to squeeze out those last picoseconds  */
/* -verify_effort specifies how much effort to spend    */
/*      making sure that the input and output designs   */
/*      are equivalent logically                        */
/*------------------------------------------------------*/
compile -map_effort medium -verify -verify_effort medium

/*------------------------------------------------------*/
/* Now trace the critical (slowest) path and see if     */
/* the timing works.                                    */
/*                                                      */
/* If the slack is NOT met, you HAVE A PROBLEM and      */
/* need to redesign or try some other minimization      */
/* tricks that Synopsys can do                          */
/*------------------------------------------------------*/
report_timing 

/********************************************************/
/*                                                      */
/* This is your section to do different things to       */
/* improve timing or area - RTFM                        */
/*                                                      */
/********************************************************/

/********************************************************/
/*                                                      */
/* Now resynthesize the design for the fastest corner   */
/* making sure that hold time conditions are met        */
/*                                                      */
/********************************************************/

/*------------------------------------------------------*/
/* Specify the fastest process corner and lowest temp   */
/* And highest (fastest) Vcc                            */
/*------------------------------------------------------*/

target_library = {"ncsulib25_best.db"}
link_library = {"ncsulib25_worst.db"}
translate

/*------------------------------------------------------*/
/* Set the design rule to 'fix hold time violations'    */
/* Then compile the design again, telling Synopsys to   */
/* Only change the design if there are hold time        */
/* violations.                                          */
/*------------------------------------------------------*/
set_fix_hold clock
compile -only_design_rule -incremental

/*------------------------------------------------------*/
/* Report the fastest path.  Make sure the hold         */
/* is actually met.                                     */
/*------------------------------------------------------*/
report_timing -delay min

/*------------------------------------------------------*/
/* Write out the 'fastest' (minimum) timing file        */
/* in Standard Delay Format.  We might use this in later*/
/* verification.                                        */
/*------------------------------------------------------*/
write_timing -output sudoku_min.sdf -format sdf

/*------------------------------------------------------*/
/* Since Synopsys has to insert logic to meet hold      */
/* violations, we might find that we have setup         */
/* violations now.  SO lets recheck with the slowest    */
/* corner etc.                                          */
/*                                                      */ 
/*  YOU have problems if the slack is NOT MET           */
/*                                                      */ 
/* 'translate' means 'translate to new library'         */
/*------------------------------------------------------*/

target_library = {"ncsulib25_worst.db"}
link_library = {"ncsulib25_worst.db"}
translate

report_timing




/* REPORT AREA */

/* current_design = top; */
report_area

/*------------------------------------------------------*/
/* Write out the resulting netlist in Verliog format    */
/*------------------------------------------------------*/
write -f verilog -o sudoku_final.v

/*------------------------------------------------------*/
/* Write out the resulting heirarchial netlist          */
/* in Verliog format. We will need this      	      	*/  
/* for Silicon Ensemble       	             	        */
/*------------------------------------------------------*/

write -hierarchy -format verilog -output sudoku_heirarchy.v

/*------------------------------------------------------*/
/* Write out the 'slowest' (maximum) timing file        */
/* in Standard Delay Format.  We might use this in later*/
/* verification.                                        */
/*------------------------------------------------------*/
write_timing -output sudoku_max.sdf -format sdf


