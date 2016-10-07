/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2014 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.tooling.netbeans.sightly.editor;

import javax.swing.text.Document;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.filesystems.FileObject;

/**
 *
 * @author amuthmann
 */
public class SightlyEditorUtil {

  /**
   *
   * @param doc
   * @return
   */
  public static JavaSource getJavaSource(Document doc) {
    FileObject fileObject = NbEditorUtilities.getFileObject(doc);
    if (fileObject == null) {
      return null;
    }
    Project project = FileOwnerQuery.getOwner(fileObject);
    if (project == null) {
      return null;
    }
    SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups("java");
    for (SourceGroup sourceGroup : sourceGroups) {
      return JavaSource.create(ClasspathInfo.create(sourceGroup.getRootFolder()));
    }
    return null;
  }
}
