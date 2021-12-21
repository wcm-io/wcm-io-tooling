File file = new File( basedir, "build.log" );
assert file.exists();

String text = file.getText("utf-8");

assert text.contains("node: '14.18.2'")
assert text.contains("npm: '7.0.2'")

return true;
