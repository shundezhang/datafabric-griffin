#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
This module is intended to provide a simple acceptance test automation
against the Griffin GridFTP server.

The following requirements need to be met:

 * Currently valid Grid proxy certificate available.
 * Availability of UberFTP in the PATH of the user executing this.
"""

## Created: 2010-12-15 Guy K. Kloss <Guy.Kloss@aut.ac.nz>
##
## Copyright 2010 Australian Research Collaboration Service
##                and Auckland University of Technology, New Zealand
##
## Some rights reserved.
##
## http://www.arcs.org.au/
## http://www.aut.ac.nz/

import os
import test_config

def execute(command):
    """
    Executes the command given in a shell, and returns all output from
    STDIN and STDERR as a string.

    @param command: Command to execute (incl. calling parameters).
    @type command: C{string}
    """
    prepared_command = command % test_config.REPLACEMENTS
    try:
        import subprocess
        _execute_subprocess(prepared_command)
    except ImportError:
        _execute_popen(prepared_command)


def _execute_subprocess(command):    
    """
    Executes using the (newer) preferred subprocess module.
    """
    import subprocess
    the_process = subprocess.Popen([command],
                                   shell=True,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.STDOUT,
                                   close_fds=True)
    print ''.join(the_process.stdout.readlines())


def _execute_popen(command):
    """
    Executes using the (older) now deprecated os.popen style.
    """
    (_, child_stdout_and_stderr) = os.popen4(command)
    print ''.join(child_stdout_and_stderr.readlines())


def _find_test_files():
    """
    Finds all test files (.txt files in the TESTS_DIR), and returns
    them as a list.
    """
    text_files = [x
                  for x in os.listdir(test_config.TESTS_DIR)
                  if x.endswith('.txt')]
    test_files = [os.path.join(test_config.TESTS_DIR, x)
                  for x in text_files]
    return test_files


def _test(test_runner):
    import doctest
    import unittest
    test_suite = doctest.DocFileSuite(_find_test_files()[0])
    test_runner.run(test_suite)
    
    # doctest.NORMALIZE_WHITESPACE
    # doctest.testmod()
    # Or put tests into an external text file and use
    # doctest.DocFileSuite()
    # See: http://en.wikipedia.org/wiki/Doctest
    # and: http://docs.python.org/library/doctest.html

    
if __name__ == '__main__':
    import unittest
    test_runner = unittest.TextTestRunner()
    _test(test_runner)