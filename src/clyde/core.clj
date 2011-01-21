(ns clyde.core
  (:import (javax.swing JFrame JLabel JPanel JTextArea JScrollPane
                        JMenuBar JMenu JMenuItem KeyStroke JSplitPane
                        SpringLayout)
           (javax.swing.event CaretListener)
           (javax.swing.text DefaultHighlighter
                             DefaultHighlighter$DefaultHighlightPainter)
           (java.awt.event ActionListener KeyEvent)
           (java.awt Color Font Toolkit FileDialog)
           (java.io File FilenameFilter FileReader FileWriter OutputStream))
  (:use [clojure.contrib.duck-streams :only (writer)])
  (:gen-class))

(defn get-mono-font []
  (Font. "Monaco" Font/PLAIN 11))

(defn make-text-area []
  (doto (JTextArea.)
    (.setFont (get-mono-font))))

(defn get-caret-position [doc]
  (let [t (:doc-text-area doc)
        offset (.getCaretPosition t)
        row (.getLineOfOffset t offset)
        col (- offset (.getLineStartOffset t row))]
    {:row row :col col}))

(defn display-caret-position [doc]
  (let [{:keys [row col]} (get-caret-position doc)]
    (.setText (:status-bar doc) (str " " (inc row) "|" (inc col)))))
  
(defn highlight
  ([doc start stop color]
    (.addHighlight (.getHighlighter (:doc-text-area doc))
                   start stop
                   (DefaultHighlighter$DefaultHighlightPainter. color)))
  ([doc pos color] (highlight doc pos (inc pos) color)))

(defn remove-highlight
  ([doc highlight-object]
    (.removeHighlight (.getHighlighter (:doc-text-area doc))
                      highlight-object)))

(defn make-scroll-pane [text-area]
    (JScrollPane. text-area))

(defn make-split-pane []
  (JSplitPane. JSplitPane/VERTICAL_SPLIT true))

(defn put-constraint [comp1 edge1 comp2 edge2 dist]
  (let [edges {:n SpringLayout/NORTH
               :e SpringLayout/EAST
               :s SpringLayout/SOUTH
               :w SpringLayout/WEST}]
  (.. comp1 getParent getLayout
            (putConstraint (edges edge1) comp1 
                           dist (edges edge2) comp2))))

(defn put-constraints [comp & args]
  (let [args (partition 3 args)
        edges [:n :w :s :e]]
    (dorun (map #(apply put-constraint comp %1 %2) edges args))))

(defn constrain-to-parent [comp & args]
  (apply put-constraints comp
        (flatten (map #(cons (.getParent comp) %) (partition 2 args)))))

(defn create-doc []
  (let [doc-text-area (make-text-area)
        repl-text-area (make-text-area)
        split-pane (make-split-pane)
        status-bar (JLabel.)
        f (JFrame.)
        cp (.getContentPane f)
        layout (SpringLayout.)
        doc {:doc-text-area doc-text-area :repl-text-area repl-text-area
             :split-pane split-pane :status-bar status-bar :frame f
             :file (atom nil)}]
    (doto f
      (.setBounds 50 50 400 400)
      (.setLayout layout)
      (.add split-pane)
      (.add status-bar))
    (doto status-bar
      (.setFont (Font. "Courier" Font/PLAIN 13)))
    (constrain-to-parent split-pane :n 0 :w 0 :s -16 :e 0)
    (constrain-to-parent status-bar :s -16 :w 0 :s 0 :e 0)
    (.layoutContainer layout f)
    (doto doc-text-area
      (.addCaretListener
        (reify CaretListener
          (caretUpdate [this evt] (display-caret-position doc)))))
    (doto split-pane
      (.add (make-scroll-pane doc-text-area))
      (.add (make-scroll-pane repl-text-area))
      (.setResizeWeight 1.0))      
    doc))

(defn choose-file [frame suffix load]
  (let [dialog
    (doto (FileDialog. frame "Open clojure file"
            (if load FileDialog/LOAD FileDialog/SAVE))
      (.setFilenameFilter
        (reify FilenameFilter
          (accept [this _ name] (. name endsWith suffix))))
      .show)
    d (.getDirectory dialog)
    n (.getFile dialog)]
    (File. d n)))
    
(defn open-file [doc suffix]
  (let [frame (doc :frame)
        file (choose-file frame suffix true)]
    (.read (doc :doc-text-area) (FileReader. (.getAbsolutePath file)) nil)
    (.setTitle frame (.getPath file))
    (reset! (doc :file) file)))

(defn save-file [doc]
  (.write (doc :doc-text-area) (FileWriter. @(doc :file))))

(defn save-file-as [doc]
  (let [frame (doc :frame)
        file (choose-file frame ".clj" false)]
    (reset! (doc :file) file)
    (save-file doc)
    (.setTitle frame (.getPath file))))
  
(defn add-menu-item [menu item-name key-shortcut response-fn]
  (.add menu
    (doto (JMenuItem. item-name)
      (.setAccelerator (KeyStroke/getKeyStroke key-shortcut
        (. (Toolkit/getDefaultToolkit) getMenuShortcutKeyMask)))
      (.addActionListener
        (reify ActionListener
          (actionPerformed [this action-event]
            (do (response-fn))))))))

(defn make-menus [doc]
  (System/setProperty "apple.laf.useScreenMenuBar" "true")
  (let [menu-bar (JMenuBar.)
        file-menu (JMenu. "File")]
     (. (doc :frame) setJMenuBar menu-bar)
     (add-menu-item file-menu "Open" \O #(reset! (doc :file) (open-file doc ".clj")))
     (add-menu-item file-menu "Save" \S #(save-file doc))
     (add-menu-item file-menu "Save as..." \R #(save-file-as doc))
     (. menu-bar add file-menu)))

(defn startup []
  (let [doc (create-doc)]
     (def current-doc doc)
     (make-menus doc)
     (.show (doc :frame))))

(defn -main [& args]
  (startup))
