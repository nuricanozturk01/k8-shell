package main

import (
	"context"
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"

	"github.com/nsf/termbox-go"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
)

type screen int

const (
	configScreen screen = iota
	namespaceScreen
	podScreen
)

var (
	currentScreen screen
	clientset     *kubernetes.Clientset
	configFiles   []string
	namespaces    []string
	pods          []string
	selectedIndex int
)

func main() {
	// Termbox'u başlatma
	err := termbox.Init()
	if err != nil {
		log.Fatalf("Termbox başlatılamadı: %v", err)
	}
	defer termbox.Close()

	// Kubeconfig dosyalarını listeleyin
	configFiles, err = listKubeConfigs()
	if err != nil {
		log.Fatalf("Config dosyaları alınamadı: %v", err)
	}

	// Başlangıç ekranı
	currentScreen = configScreen
	printCurrentScreen()

	// Navigasyon ve seçim işlemi
	waitForInput()
}

// .kube dizinindeki config dosyalarını listeleme
func listKubeConfigs() ([]string, error) {
	kubeDir := filepath.Join(os.Getenv("HOME"), ".kube")
	files, err := ioutil.ReadDir(kubeDir)
	if err != nil {
		return nil, err
	}

	var configs []string
	for _, file := range files {
		if !file.IsDir() {
			configs = append(configs, filepath.Join(kubeDir, file.Name()))
		}
	}
	return configs, nil
}

// Navigasyon ve seçim işlemi
func waitForInput() {
	for {
		switch ev := termbox.PollEvent(); ev.Type {
		case termbox.EventKey:
			switch ev.Key {
			case termbox.KeyArrowUp:
				if selectedIndex > 0 {
					selectedIndex--
					printCurrentScreen()
				}
			case termbox.KeyArrowDown:
				if selectedIndex < getCurrentItemsCount()-1 {
					selectedIndex++
					printCurrentScreen()
				}
			case termbox.KeyEnter:
				handleSelection()
			case termbox.KeyBackspace, termbox.KeyBackspace2, 'b':
				handleBack()
			case termbox.KeyCtrlC, termbox.KeyEsc:
				return
			}
		}
	}
}

// Seçim işlemi (config, namespace veya pod)
func handleSelection() {
	switch currentScreen {
	case configScreen:
		loadNamespaces(configFiles[selectedIndex])
	case namespaceScreen:
		loadPods(namespaces[selectedIndex])
	case podScreen:
		fmt.Printf("Seçilen pod: %s\n", pods[selectedIndex])
	}
}

// Geri dönme işlemi
func handleBack() {
	switch currentScreen {
	case namespaceScreen:
		currentScreen = configScreen
	case podScreen:
		currentScreen = namespaceScreen
	}
	selectedIndex = 0
	printCurrentScreen()
}

// Config dosyası ile Kubernetes client oluşturma
func loadNamespaces(configPath string) {
	var err error
	clientset, err = createKubernetesClient(configPath)
	if err != nil {
		log.Fatalf("Kubernetes client oluşturulamadı: %v", err)
	}

	// Namespace'leri alma
	namespaceList, err := clientset.CoreV1().Namespaces().List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Namespace'ler alınamadı: %v", err)
	}

	// Namespace isimlerini saklama
	namespaces = make([]string, len(namespaceList.Items))
	for i, ns := range namespaceList.Items {
		namespaces[i] = ns.Name
	}

	currentScreen = namespaceScreen
	selectedIndex = 0
	printCurrentScreen()
}

// Namespace içindeki podları yükleme
func loadPods(namespace string) {
	podList, err := clientset.CoreV1().Pods(namespace).List(context.TODO(), metav1.ListOptions{})
	if err != nil {
		log.Fatalf("Podlar alınamadı: %v", err)
	}

	// Pod isimlerini saklama
	pods = make([]string, len(podList.Items))
	for i, pod := range podList.Items {
		pods[i] = pod.Name
	}

	currentScreen = podScreen
	selectedIndex = 0
	printCurrentScreen()
}

// Ekranları güncelleme
func printCurrentScreen() {
	termbox.Clear(termbox.ColorDefault, termbox.ColorDefault)

	var items []string
	switch currentScreen {
	case configScreen:
		items = configFiles
	case namespaceScreen:
		items = namespaces
	case podScreen:
		items = pods
	}

	for i, item := range items {
		if i == selectedIndex {
			printLine(0, i, item, termbox.ColorBlack, termbox.ColorWhite)
		} else {
			printLine(0, i, item, termbox.ColorWhite, termbox.ColorDefault)
		}
	}

	termbox.Flush()
}

// Mevcut ekrandaki öğe sayısını döndürme
func getCurrentItemsCount() int {
	switch currentScreen {
	case configScreen:
		return len(configFiles)
	case namespaceScreen:
		return len(namespaces)
	case podScreen:
		return len(pods)
	}
	return 0
}

// Seçilen config dosyası ile Kubernetes client oluşturma
func createKubernetesClient(configPath string) (*kubernetes.Clientset, error) {
	config, err := clientcmd.BuildConfigFromFlags("", configPath)
	if err != nil {
		return nil, err
	}
	return kubernetes.NewForConfig(config)
}

// Metni belirtilen koordinatlara yazdırma
func printLine(x, y int, msg string, fg, bg termbox.Attribute) {
	for i, c := range msg {
		termbox.SetCell(x+i, y, c, fg, bg)
	}
}
