/********************************************************/
/*                                                      */
/* Basic NCSU Synthesis Script                          */
/*                                                      */
/* Set up for the 0.25u library                   */
/*                                                      */
/* Revision History                                     */
/*   1/5/97 : Author P. Franzon                         */
/*   1/2/98 : More heavilly commented                   */
/*                                                      */
/********************************************************/

/********************************************************/
/*                                                      */
/* Read in Verilog file and map (synthesize)            */
/* onto a generic library                               */
/*                                                      */
/* MAKE SURE THAT YOU CORRECT ALL WARNINGS THAT APPEAR  */
/* during the execution of the read command are fixed   */
/* or understood to have no impact                      */
/*                                                      */
/* ALSO CHECK your latch/flip-flop list for unintended  */
/* latches                                              */
/*                                                      */
/********************************************************/

Read -f Verilog sudoku.v


