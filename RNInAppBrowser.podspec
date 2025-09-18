require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = "RNInAppBrowser"
  s.version        = package['version']
  s.summary        = package['description']
  s.description    = package['description']
  s.license        = package['license']
  s.author         = package['author']
  s.homepage       = package['homepage']
  s.source         = { :git => package['repository']['url'], :tag => "v#{s.version}" }

  s.requires_arc   = true
  s.platform       = :ios, '13.0'

  s.preserve_paths = 'LICENSE', 'README.md', 'package.json', 'index.js'
  s.source_files   = 'ios/**/*.{h,m}'
  s.exclude_files  = 'android/**/*'

  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    install_modules_dependencies(s)
  else
    s.dependency   "React-Core"
  end

end
