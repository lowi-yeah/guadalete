#!/usr/bin/ruby

def read_file(file_name)

  result = ""
  File.open(file_name, 'r') do |f|
    while line = f.gets
      # l = line.gsub() 
      l0 = line.gsub(/,/, "\n")
      l1 = l0.gsub(/\]\s\[/, "]\n[") 
      l2 = l1.gsub(/\}\s\{/, "}\n{") 
      result << l2
    end
  end
  result
end


puts "Start"
result = read_file("tmp.clj")
puts "result #{result}"



File.write('tmp.clj', result)