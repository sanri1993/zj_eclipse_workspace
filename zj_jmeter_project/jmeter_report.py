# -*- coding: utf-8 -*-

import codecs
import os
import sys


class JmeterReport(object):
    '''
    Create report from jmeter jtl (csv) file.
    '''

    def __init__(self):
        self.print_header()

    def print_header(self):
        # duration,label,samplers,error%,avg,median,min,max,90%,95%,99%,99.9%,throughput
        header = ['%15s' % 'Duration (sec)', '%20s' % 'Label', '%10s' % 'Samples', '%10s' % 'Error%', '%10s' % 'Average', '%10s' % 'Median', '%10s' %
                  'Min', '%10s' % 'Max', '%10s' % '90%', '%10s' % '99%', '%10s' % '99.9%', '%10s' % '99.99%', '%20s' % 'Throughput (sec)']
        print('-' * 170)
        print('|'.join(header))
        print('-' * 170)

    def print(self, lines):
        self._label = ''
        self._start_ts = 0
        self._end_ts = 0
        self._count = 0
        self._errors = 0
        self._average = 0
        self._elapsed_list = []

        self._format_jtl_lines(lines)
        self._parse_and_print_body()

    def _format_jtl_lines(self, lines):
        self._label = lines[0].split(',')[2]
        self._start_ts = int(lines[0].split(',')[0])
        self._end_ts = int(lines[len(lines) - 1].split(',')[0])
        self._count = len(lines)

        sum_elapsed = 0
        for line in lines:
            fields = line.split(',')
            elapsed = int(fields[1])
            sum_elapsed += elapsed
            self._elapsed_list.append(elapsed)

            code = fields[3]
            if code != '200':
                self._errors += 1

        self._average = int(sum_elapsed / self._count)

    def _parse_and_print_body(self):
        samplers_count = self._count
        average = self._average
        duration = float((self._end_ts - self._start_ts) / 1000)  # second

        error_percent = '%.3f' % float(
            100 * self._errors / samplers_count) + '%'

        sorted_elapsed_list = sorted(self._elapsed_list)
        median = sorted_elapsed_list[int(samplers_count / 2)]
        min = sorted_elapsed_list[0]
        max = sorted_elapsed_list[samplers_count - 1]

        line_90 = sorted_elapsed_list[int(round(samplers_count * 0.9)) - 1]
        line_99 = sorted_elapsed_list[int(round(samplers_count * 0.99)) - 1]
        line_999 = sorted_elapsed_list[int(round(samplers_count * 0.999)) - 1]
        line_9999 = sorted_elapsed_list[int(
            round(samplers_count * 0.9999)) - 1]

        throughput = float(samplers_count / duration)  # tps

        line = ['%15.2f' % duration, '%20s' % self._label, '%10d' % samplers_count, '%10s' % error_percent, '%10d' % average, '%10d' % median, '%10d' % min, '%10d' % max, '%10d' %
                line_90, '%10d' % line_99, '%10d' % line_999, '%10d' % line_9999, '%20.2f' % throughput]
        print('|'.join(line))
# end JmeterReport class


def read_file(file_path):
    if not os.path.exists(file_path):
        raise Exception('File is NOT exist: ' + file_path)

    with codecs.open(file_path, 'r', 'utf-8') as f:
        return f.readlines()


def main(ftl_file_path):
    jtl_lines = read_file(ftl_file_path)

    lines_for_label = {}
    for line in jtl_lines[1:]:
        label = line.split(',')[2]
        if label in lines_for_label.keys():
            lines_for_label[label].append(line)
        else:
            lines_for_label[label] = [line]

    report = JmeterReport()
    for key in lines_for_label.keys():
        report.print(lines_for_label[key])


if __name__ == '__main__':

    jtl_file_path = sys.argv[1]
    main(jtl_file_path)

    print('Jmeter report parse Done.')
