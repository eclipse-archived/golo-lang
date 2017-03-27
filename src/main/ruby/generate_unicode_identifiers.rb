# Copyright (c) 2012-2017 Institut National des Sciences Appliquées de Lyon (INSA-Lyon)
#
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# Quick and dirty script to generate the unicode identifier regexp automatically

require 'open-uri'

UNICODE_DATA_URL = "http://www.unicode.org/Public/6.2.0/ucd/UnicodeData.txt"
CATEGORIES = %w(Lu Ll Lo Lt Nl Sc Sm So Pc)
EXCEPT = %w(+ - * / % < = > ! ( ) [ ] { } # ' " ^ | . ~ ^ \\ : ; ? @ `).map(&:ord)

def processSurrogate(code)
  if code < 0x10000
    [code, nil]
  else
    #             000uuuuuxxxxxxxxxxxxxxxx
    u = (code & 0b000111110000000000000000) >> 16
    x = (code & 0b000000001111111111111111)
    w = u - 1
    [
      0b1101100000000000 | (w << 6) | (x >> 10),
      0b1101110000000000 | (x & 0b0000001111111111)
    ]
  end
end

ranges = []
surrogateRanges = {}

from = nil
to = nil
surrogate = nil

open UNICODE_DATA_URL do |f|
  f.each_line do |l|
    entry = l.split /;/
    charcode = entry[0].to_i 16
    category = entry[2]

    high, low = processSurrogate charcode

    unless from
      if CATEGORIES.include?(category) && ! EXCEPT.include?(charcode)
        unless low
          from = to = charcode
        else
          surrogate = high
          from = to = low
        end
      end
    else
      unless CATEGORIES.include?(category) && ! EXCEPT.include?(charcode)
        unless surrogate
          STDERR.puts "#{from.inspect}..#{to.inspect}"
          ranges << (from..to)
          from = to = nil
        else
          STDERR.puts "#{from.inspect}..#{to.inspect} (#{surrogate.inspect})"
          surrogateRanges[surrogate] ||= []
          surrogateRanges[surrogate] << (from..to)
          from = to = surrogate = nil
        end
      else
        unless low
          to = charcode
        else
          if high == surrogate
            to = low
          else
            STDERR.puts "#{from.inspect}..#{to.inspect} (#{surrogate.inspect})"
            unless surrogate
              ranges << (from..to)
            else
              surrogateRanges[surrogate] ||= []
              surrogateRanges[surrogate] << (from..to)
            end

            to = nil
            surrogate = high
            from = to = low
          end
        end
      end
    end
  end
end

puts '  <#LETTER: (['
line = "    "
ranges.each do |r|
  if r.first == r.end
    s = '"\u%04X"' % r.first
  else
    s = '"\u%04X"-"\u%04X"' % [r.first, r.end]
  end

  if (line + s).size > 80
    puts line
    line = "    #{s},"
  else
    line << s << ","
  end
end
puts "#{line[0..-2]}])"
surrogateRanges.each do |s, rs|
  puts '    | ("\u%04X" [' % s
  line = "      "
  rs.each do |r|
    if r.first == r.end
      s = '"\u%04X"' % r.first
    else
      s = '"\u%04X"-"\u%04X"' % [r.first, r.end]
    end

    if (line + s).size > 80
      puts line
      line = "      #{s},"
    else
      line << s << ","
    end
  end
  puts "#{line[0..-2]}])"
end
puts '  >'
puts '  |'
puts '  <#ID_REST: ["0"-"9"] | <LETTER> >'
