#!/usr/bin/perl -w
#use strict;

@ARGV == 1
  or die "Usage: $0 <file> <number-of-parts>";
 
my $infilename = shift @ARGV;

open my $INFILE, '<', $infilename
	or die "Could not reopen $infilename for reading";

my(@lines) = <$INFILE>; # read file into list

my($line);
my $N = 0;
foreach $line (@lines) # loop thru list
 	{
 	++$N;
 	open my $OUTFILE, '>', "runjob.$N.sh"
		or die "Could not open runjob.$N.sh for writing";

#		print "$N $line";
		print $OUTFILE "$N $line";
	}
close($INFILE);

