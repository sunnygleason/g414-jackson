#
# Examples:
#
# {
#   "foo":"bar",
#   "b":["embedded", "array", {"ok":[1, 2, 3], "notok":false}, {"foo":{"bar":"baz"}}],
#   "a":["embedded", "array"],
#   "another":{"deeply":"nested","object":true},
#   "now":1
# }
#
#

require 'java'
require 'pp'

java_import 'org.codehaus.jackson.JsonFactory'
java_import 'org.codehaus.jackson.JsonParser'

java_import 'com.g414.jackson.path.JsonMatcher'
java_import 'com.g414.jackson.path.ExtendedJsonPattern'

c = ExtendedJsonPattern::Condition
 
infile = ARGV.shift

find   = ExtendedJsonPattern::Builder.new
find.add([c::newEquals(nil, "{")])
find.add([
    c::newHaveSeen("foo", "bar"),
    c::newEquals("b", nil)
    ])
find.add([c::newEquals(nil, "[")])
find.add([c::newEquals(nil, "{")])
find.add([c::newHaveSeen("ok", "*")])


p = JsonFactory.new.createJsonParser(java::io::File.new(infile))
m = JsonMatcher.new(p, find.build(), true)

m.each do |v|
  puts v.context.to_s + "\t\t" + v.to_s
end


