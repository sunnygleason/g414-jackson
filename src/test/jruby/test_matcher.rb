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
# jruby test_driver.rb sample.json.txt {
# jruby test_driver.rb sample.json.txt { another { "*" "true"
# jruby test_driver.rb sample.json.txt { b [ {
# jruby test_driver.rb sample.json.txt { b [ "*"
#
#

require 'java'
require 'pp'

java_import 'org.codehaus.jackson.JsonFactory'
java_import 'org.codehaus.jackson.JsonParser'

java_import 'com.g414.jackson.path.JsonMatcher'
java_import 'com.g414.jackson.path.SimpleJsonPattern'


infile = ARGV.shift
find   = SimpleJsonPattern.new(ARGV, false)

p = JsonFactory.new.createJsonParser(java::io::File.new(infile))
m = JsonMatcher.new(p, find, true)

m.each do |v|
  puts v.context.to_s + "\t\t" + v.to_s
end


